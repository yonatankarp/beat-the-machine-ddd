package com.yonatankarp.beatthemachine.domain.valueobject

import com.yonatankarp.beatthemachine.domain.exception.InvalidGuess
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

val GuessSuite by testSuite {
    given("constructing a Guess") {
        whenever("the word is blank") {
            then("it is rejected") {
                shouldThrow<InvalidGuess> { Guess("   ") }
            }
        }
        whenever("the word is empty") {
            then("it is rejected") {
                shouldThrow<InvalidGuess> { Guess("") }
            }
        }
    }

    given("a Guess with mixed case") {
        whenever("normalized") {
            then("trims and lowercases") {
                Guess("Hello").normalized() shouldBe "hello"
            }
        }
    }
}
