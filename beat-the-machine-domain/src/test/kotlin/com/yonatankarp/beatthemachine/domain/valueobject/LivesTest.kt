package com.yonatankarp.beatthemachine.domain.valueobject

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LivesTest {
    @Test
    fun `cannot be negative`() {
        assertFailsWith<IllegalArgumentException> { Lives(-1) }
    }

    @Test
    fun `lose decrements and floors at zero`() {
        assertEquals(Lives(0), Lives(1).lose())
        assertEquals(Lives(0), Lives(0).lose())
    }

    @Test
    fun `is exhausted at zero`() {
        assertTrue(Lives(0).isExhausted())
        assertFalse(Lives(1).isExhausted())
    }

    @Test
    fun `lives scale with word count and difficulty multiplier`() {
        // base = 3 lives per word, EASY x1.5 / MEDIUM x1.0 / HARD x0.7, rounded, floor 2
        assertEquals(Lives(9), Lives.forSecret(Prompt("dragon cookie"), Difficulty.EASY)) // round(3*2*1.5)=9
        assertEquals(Lives(9), Lives.forSecret(Prompt("a b c"), Difficulty.MEDIUM)) // round(3*3*1.0)=9
        assertEquals(Lives(8), Lives.forSecret(Prompt("a b c d"), Difficulty.HARD)) // round(3*4*0.7)=8
    }

    @Test
    fun `lives never drop below the floor`() {
        assertEquals(Lives(2), Lives.forSecret(Prompt("cat"), Difficulty.HARD)) // round(3*1*0.7)=2
    }
}
