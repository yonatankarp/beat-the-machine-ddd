package com.yonatankarp.beatthemachine.domain.valueobject

import com.yonatankarp.beatthemachine.test.dsl.asGuess
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

val MaskedPromptSuite by testSuite {
    given("a two-word prompt with no guesses") {
        whenever("masking") {
            then("all tokens are hidden") {
                val masked = MaskedPrompt.of("hello world".asPrompt(), emptySet())
                masked.tokens shouldBe listOf(MaskedToken.Hidden(5), MaskedToken.Hidden(5))
            }
            then("the prompt is not fully revealed") {
                val masked = MaskedPrompt.of("hello world".asPrompt(), emptySet())
                masked.isFullyRevealed().shouldBeFalse()
            }
        }
    }

    given("a prompt with one word guessed") {
        whenever("masking") {
            then("the first token is revealed") {
                val masked = MaskedPrompt.of("Hello World".asPrompt(), setOf("hello".asGuess()))
                masked.tokens[0] shouldBe MaskedToken.Revealed("Hello")
            }
            then("the second token is hidden") {
                val masked = MaskedPrompt.of("Hello World".asPrompt(), setOf("hello".asGuess()))
                masked.tokens[1] shouldBe MaskedToken.Hidden(5)
            }
        }
    }

    given("a prompt with a repeated word guessed") {
        whenever("masking") {
            then("both occurrences are revealed and batman is hidden") {
                val masked = MaskedPrompt.of("na na batman".asPrompt(), setOf("na".asGuess()))
                masked.tokens shouldBe
                    listOf(
                        MaskedToken.Revealed("na"),
                        MaskedToken.Revealed("na"),
                        MaskedToken.Hidden(6),
                    )
            }
        }
    }

    given("a prompt with mixed whitespace") {
        whenever("masking with one word guessed") {
            then("produces exactly 2 tokens") {
                val masked = MaskedPrompt.of("hello\t \nworld".asPrompt(), setOf("world".asGuess()))
                masked.tokens.size shouldBe 2
            }
            then("the last token is revealed") {
                val masked = MaskedPrompt.of("hello\t \nworld".asPrompt(), setOf("world".asGuess()))
                masked.tokens[1] shouldBe MaskedToken.Revealed("world")
            }
        }
    }

    given("all words guessed") {
        whenever("masking") {
            then("isFullyRevealed is true") {
                val masked =
                    MaskedPrompt.of(
                        "hello world".asPrompt(),
                        setOf("hello".asGuess(), "world".asGuess()),
                    )
                masked.isFullyRevealed().shouldBeTrue()
            }
        }
    }
}
