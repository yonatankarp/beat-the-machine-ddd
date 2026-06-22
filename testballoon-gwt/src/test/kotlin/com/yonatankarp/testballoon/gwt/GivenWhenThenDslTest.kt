package com.yonatankarp.testballoon.gwt

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val GivenWhenThenDslSuite by testSuite {
    given("setup") {
        val events by setup { mutableListOf("given") }

        whenever("action") {
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
}
