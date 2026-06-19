package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryChallengeTemplates
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ChallengeTemplateSeederTest {
    private val templates = InMemoryChallengeTemplates()

    @Test
    fun `seeds every difficulty from SEED and is idempotent`() =
        runTest {
            var n = 0
            val seeder = ChallengeTemplateSeeder(templates) { "seed-${n++}" }

            seeder.seed()
            val easyAfterFirst = templates.count(Difficulty.EASY)
            seeder.seed() // second run must not double up

            assertEquals(SEED.size, easyAfterFirst)
            assertEquals(SEED.size, templates.count(Difficulty.EASY))
            assertEquals(SEED.size, templates.count(Difficulty.HARD))
        }
}
