package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplate
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryChallengeTemplatesTest {
    private val templates = InMemoryChallengeTemplates()

    @Test
    fun `save then count and randomReady by difficulty`() =
        runTest {
            templates.save(ChallengeTemplate("1", Difficulty.EASY, Prompt("red car"), "/images/a"))
            templates.save(ChallengeTemplate("2", Difficulty.HARD, Prompt("a b c"), "/images/b"))
            assertEquals(1, templates.count(Difficulty.EASY))
            assertEquals(0, templates.count(Difficulty.MEDIUM))
            assertEquals(Prompt("red car"), templates.randomReady(Difficulty.EASY)!!.prompt)
            assertNull(templates.randomReady(Difficulty.MEDIUM))
        }

    @Test
    fun `randomReady only returns the requested difficulty`() =
        runTest {
            repeat(5) { templates.save(ChallengeTemplate("h$it", Difficulty.HARD, Prompt("x y z"), "/images/$it")) }
            repeat(20) { assertTrue(templates.randomReady(Difficulty.HARD)!!.difficulty == Difficulty.HARD) }
        }
}
