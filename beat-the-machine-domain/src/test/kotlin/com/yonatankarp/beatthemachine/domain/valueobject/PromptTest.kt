package com.yonatankarp.beatthemachine.domain.valueobject

import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

val PromptSuite by testSuite {
    given("splitting a prompt into words") {
        whenever("the text is separated by a single space") {
            then("it splits into the words") {
                Prompt("hello world").words() shouldBe listOf("hello", "world")
            }
        }
        whenever("the text uses mixed whitespace") {
            then("it still splits into the words") {
                Prompt("a\t b\n c").words() shouldBe listOf("a", "b", "c")
            }
        }
    }

    given("constructing a Prompt") {
        whenever("the text is blank") {
            then("it is rejected") { shouldThrow<IllegalArgumentException> { Prompt("   ") } }
        }
        whenever("the text is empty") {
            then("it is rejected") { shouldThrow<IllegalArgumentException> { Prompt("") } }
        }
    }
}
