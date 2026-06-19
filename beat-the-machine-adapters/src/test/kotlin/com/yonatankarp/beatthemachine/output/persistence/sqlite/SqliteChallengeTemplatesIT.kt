package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplate
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SqliteChallengeTemplatesIT {
    private val jdbc = newSqliteJdbc()
    private val templates = SqliteChallengeTemplates(jdbc)

    @Test
    fun `save, count and randomReady round-trip by difficulty`() =
        runTest {
            templates.save(ChallengeTemplate("1", Difficulty.EASY, Prompt("red car"), "/images/a"))
            templates.save(ChallengeTemplate("2", Difficulty.EASY, Prompt("blue boat"), "/images/b"))
            templates.save(ChallengeTemplate("3", Difficulty.HARD, Prompt("a b c"), "/images/c"))
            assertEquals(2, templates.count(Difficulty.EASY))
            assertEquals(1, templates.count(Difficulty.HARD))
            assertEquals(Difficulty.HARD, templates.randomReady(Difficulty.HARD)!!.difficulty)
        }

    @Test
    fun `randomReady returns null for an empty difficulty`() =
        runTest {
            assertNull(templates.randomReady(Difficulty.MEDIUM))
        }
}
