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
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val inFlight = ConcurrentHashMap.newKeySet<Difficulty>()

    fun warmUp() = Difficulty.entries.forEach { replenish(it) }

    fun replenish(difficulty: Difficulty) {
        if (!inFlight.add(difficulty)) return
        scope.launch {
            try {
                var deficit = target - (templates count difficulty)
                while (deficit-- > 0) {
                    val prompt = promptSource answer PromptSource.Query(difficulty)
                    when (val picture = machine answer Machine.Query(prompt)) {
                        is Picture.Ready -> {
                            templates save ChallengeTemplate(idFactory(), difficulty, prompt, picture.url)
                        }

                        else -> {
                            logger.warn("Skipping failed template generation for {}", difficulty)
                        }
                    }
                }
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
