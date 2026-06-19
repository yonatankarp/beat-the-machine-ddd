package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplate
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import com.yonatankarp.beatthemachine.test.dsl.given
import com.yonatankarp.beatthemachine.test.dsl.then
import com.yonatankarp.beatthemachine.test.dsl.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

val SqliteChallengeTemplatesITSuite by testSuite {
    given("a SQLite template pool") {
        whenever("saving templates and reloading them") {
            then("count and randomReady round-trip by difficulty") {
                val templates = SqliteChallengeTemplates(newSqliteJdbc())
                templates.save(ChallengeTemplate("1", Difficulty.EASY, Prompt("red car"), "/images/a"))
                templates.save(ChallengeTemplate("2", Difficulty.EASY, Prompt("blue boat"), "/images/b"))
                templates.save(ChallengeTemplate("3", Difficulty.HARD, Prompt("a b c"), "/images/c"))
                templates.count(Difficulty.EASY) shouldBe 2
                templates.count(Difficulty.HARD) shouldBe 1
                templates.randomReady(Difficulty.HARD)!!.difficulty shouldBe Difficulty.HARD
            }
        }

        whenever("a difficulty has no templates") {
            then("randomReady returns null") {
                val templates = SqliteChallengeTemplates(newSqliteJdbc())
                templates.randomReady(Difficulty.MEDIUM).shouldBeNull()
            }
        }
    }
}
