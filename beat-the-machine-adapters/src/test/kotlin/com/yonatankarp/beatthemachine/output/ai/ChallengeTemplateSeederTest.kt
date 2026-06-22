package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryChallengeTemplates
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val ChallengeTemplateSeederSuite by testSuite {
    given("a seeder over an empty pool") {
        whenever("seeding twice") {
            then("it seeds every difficulty from SEED and is idempotent") {
                val templates = InMemoryChallengeTemplates()
                var n = 0
                val seeder = ChallengeTemplateSeeder(templates) { "seed-${n++}" }

                seeder.seed()
                val easyAfterFirst = templates.count(Difficulty.EASY)
                seeder.seed()

                easyAfterFirst shouldBe SEED.size
                templates.count(Difficulty.EASY) shouldBe SEED.size
                templates.count(Difficulty.HARD) shouldBe SEED.size
            }
        }
    }
}
