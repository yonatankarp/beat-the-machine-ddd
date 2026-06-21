package com.yonatankarp.beatthemachine.domain.valueobject

import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.dsl.given
import com.yonatankarp.beatthemachine.test.dsl.then
import com.yonatankarp.beatthemachine.test.dsl.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

val LivesSuite by testSuite {
    given("constructing Lives") {
        whenever("count is negative") {
            then("it is rejected") {
                shouldThrow<IllegalArgumentException> { Lives(-1) }
            }
        }
    }

    given("Lives with one remaining") {
        whenever("losing") {
            then("decrements to zero") {
                Lives(1).lose() shouldBe Lives(0)
            }
        }
    }

    given("Lives at zero") {
        whenever("losing") {
            then("floors at zero") {
                Lives(0).lose() shouldBe Lives(0)
            }
        }
    }

    given("Lives count") {
        whenever("checking isExhausted") {
            then("returns true at zero") {
                Lives(0).isExhausted().shouldBeTrue()
            }
            then("returns false above zero") {
                Lives(1).isExhausted().shouldBeFalse()
            }
        }
    }

    given("scaling lives with forSecret") {
        val twoWordPrompt = "hello world".asPrompt()

        whenever("difficulty is EASY") {
            then("scales up") {
                Lives.forSecret(twoWordPrompt, Difficulty.EASY) shouldBe Lives(9)
            }
        }
        whenever("difficulty is MEDIUM") {
            then("scales moderately") {
                Lives.forSecret(twoWordPrompt, Difficulty.MEDIUM) shouldBe Lives(6)
            }
        }
        whenever("difficulty is HARD") {
            then("scales down") {
                Lives.forSecret(twoWordPrompt, Difficulty.HARD) shouldBe Lives(4)
            }
        }
        whenever("a very short secret would produce fewer than MIN_LIVES") {
            then("floors at MIN_LIVES") {
                Lives.forSecret("x".asPrompt(), Difficulty.HARD) shouldBe Lives(2)
            }
        }
    }
}
