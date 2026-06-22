package com.yonatankarp.testballoon.gwt

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay

val GivenWhenThenDslSuite by testSuite {
    given("a setup value") {
        val events by setup { mutableListOf("given") }

        whenever("an action is declared in the when scope") {
            val result by action {
                events += "when"
                events
            }

            then("first assertion sees only its own setup") {
                result += "first then"
                result shouldBe listOf("given", "when", "first then")
            }

            then("second assertion sees only its own setup") {
                result += "second then"
                result shouldBe listOf("given", "when", "second then")
            }
        }
    }

    given("a suspend setup value") {
        val initial by setup {
            delay(1)
            "given"
        }

        whenever("a suspend action is declared in the when scope") {
            val result by action {
                val base = initial
                delay(1)
                "$base then when"
            }

            then("the assertion can read the suspend action result") {
                result shouldBe "given then when"
            }
        }
    }
}
