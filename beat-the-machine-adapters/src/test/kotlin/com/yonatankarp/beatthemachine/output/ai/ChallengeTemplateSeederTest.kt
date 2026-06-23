package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplate
import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplates
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryChallengeTemplates
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

val ChallengeTemplateSeederSuite by testSuite {
    given("a seeder over an empty pool") {
        whenever("seeding twice") {
            then("it seeds ten templates per difficulty and is idempotent") {
                val templates = InMemoryChallengeTemplates()
                val seeder = ChallengeTemplateSeeder(templates)

                seeder.seed()
                val easyAfterFirst = templates.count(Difficulty.EASY)
                seeder.seed()

                easyAfterFirst shouldBe 10
                templates.count(Difficulty.EASY) shouldBe 10
                templates.count(Difficulty.MEDIUM) shouldBe 10
                templates.count(Difficulty.HARD) shouldBe 10
            }

            then("it uses stable ids from the seed catalog") {
                val templates = RecordingChallengeTemplates()
                val seeder = ChallengeTemplateSeeder(templates)

                seeder.seed()

                templates.saved.map { it.id } shouldContain SEED.first().id
            }
        }
    }
}

private class RecordingChallengeTemplates : ChallengeTemplates {
    val saved = mutableListOf<ChallengeTemplate>()

    override suspend fun save(template: ChallengeTemplate): ChallengeTemplate {
        saved += template
        return template
    }

    override suspend fun randomReady(difficulty: Difficulty): ChallengeTemplate? = null

    override suspend fun count(difficulty: Difficulty): Int = 0
}
