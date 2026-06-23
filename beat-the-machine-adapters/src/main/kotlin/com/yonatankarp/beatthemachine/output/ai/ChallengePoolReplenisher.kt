package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplate
import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplates
import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ChallengePoolReplenisher(
    private val promptSource: PromptSource,
    private val machine: Machine,
    private val templates: ChallengeTemplates,
    private val scope: CoroutineScope,
    private val target: Int,
    private val observe: (PoolReplenishmentEvent) -> Unit = {},
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val inFlight = ConcurrentHashMap.newKeySet<Difficulty>()

    fun warmUp() = Difficulty.entries.forEach { replenish(it) }

    fun replenish(difficulty: Difficulty) {
        if (!inFlight.add(difficulty)) return
        scope.launch {
            try {
                val start = templates count difficulty
                var current = start
                var deficit = target - start

                fun remaining(): Int = (target - current).coerceAtLeast(0)

                observe(PoolReplenishmentEvent.Started(difficulty, current, target, deficit.coerceAtLeast(0)))
                logger.info(
                    "Pool replenish started for {}: current={}, target={}, deficit={}",
                    difficulty,
                    current,
                    target,
                    deficit.coerceAtLeast(0),
                )
                while (deficit-- > 0) {
                    val prompt = promptSource answer PromptSource.Query(difficulty)
                    when (val picture = machine answer Machine.Query(prompt)) {
                        is Picture.Ready -> {
                            templates save ChallengeTemplate(idFactory(), difficulty, prompt, picture.url)
                            current += 1
                            observe(
                                PoolReplenishmentEvent.Generated(
                                    difficulty,
                                    current,
                                    target,
                                    remaining(),
                                ),
                            )
                            logger.info(
                                "Pool replenish generated for {}: current={}, target={}, remaining={}",
                                difficulty,
                                current,
                                target,
                                remaining(),
                            )
                        }

                        else -> {
                            logger.warn("Skipping failed template generation for {}", difficulty)
                        }
                    }
                }
                observe(PoolReplenishmentEvent.Finished(difficulty, current, target))
                logger.info("Pool replenish finished for {}: current={}, target={}", difficulty, current, target)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error("Pool replenish failed for {}", difficulty, e)
            } finally {
                inFlight.remove(difficulty)
            }
        }
    }
}

sealed interface PoolReplenishmentEvent {
    val difficulty: Difficulty
    val current: Int
    val target: Int

    data class Started(
        override val difficulty: Difficulty,
        override val current: Int,
        override val target: Int,
        val deficit: Int,
    ) : PoolReplenishmentEvent

    data class Generated(
        override val difficulty: Difficulty,
        override val current: Int,
        override val target: Int,
        val remaining: Int,
    ) : PoolReplenishmentEvent

    data class Finished(
        override val difficulty: Difficulty,
        override val current: Int,
        override val target: Int,
    ) : PoolReplenishmentEvent
}
