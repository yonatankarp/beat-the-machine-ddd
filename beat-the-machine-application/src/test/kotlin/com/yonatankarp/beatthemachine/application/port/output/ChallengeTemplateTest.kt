package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val ChallengeTemplateSuite by testSuite {
    given("a challenge template") {
        whenever("constructed with a difficulty, prompt and picture url") {
            then("it carries the difficulty, prompt and picture url") {
                val t = ChallengeTemplate("id-1", Difficulty.EASY, Prompt("red car"), "/images/abc")
                t.difficulty shouldBe Difficulty.EASY
                t.prompt shouldBe Prompt("red car")
                t.pictureUrl shouldBe "/images/abc"
            }
        }
    }
}
