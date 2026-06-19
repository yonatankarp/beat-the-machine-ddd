package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplate
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import com.yonatankarp.beatthemachine.test.dsl.given
import com.yonatankarp.beatthemachine.test.dsl.then
import com.yonatankarp.beatthemachine.test.dsl.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

val InMemoryChallengeTemplatesSuite by testSuite {
    given("an in-memory template pool") {
        whenever("saving templates of different difficulties") {
            then("count and randomReady are partitioned by difficulty") {
                val templates = InMemoryChallengeTemplates()
                templates.save(ChallengeTemplate("1", Difficulty.EASY, Prompt("red car"), "/images/a"))
                templates.save(ChallengeTemplate("2", Difficulty.HARD, Prompt("a b c"), "/images/b"))
                templates.count(Difficulty.EASY) shouldBe 1
                templates.count(Difficulty.MEDIUM) shouldBe 0
                templates.randomReady(Difficulty.EASY)!!.prompt shouldBe Prompt("red car")
                templates.randomReady(Difficulty.MEDIUM).shouldBeNull()
            }
        }

        whenever("several templates of one difficulty exist") {
            then("randomReady only returns the requested difficulty") {
                val templates = InMemoryChallengeTemplates()
                repeat(5) { templates.save(ChallengeTemplate("h$it", Difficulty.HARD, Prompt("x y z"), "/images/$it")) }
                repeat(20) { templates.randomReady(Difficulty.HARD)!!.difficulty shouldBe Difficulty.HARD }
            }
        }
    }
}
