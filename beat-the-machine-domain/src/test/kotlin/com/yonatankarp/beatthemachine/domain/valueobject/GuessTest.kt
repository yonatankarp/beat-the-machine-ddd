package com.yonatankarp.beatthemachine.domain.valueobject

import com.yonatankarp.beatthemachine.domain.exception.InvalidGuess
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GuessTest {
    @Test
    fun `rejects blank word`() {
        assertFailsWith<InvalidGuess> { Guess("   ") }
    }

    @Test
    fun `rejects empty word`() {
        assertFailsWith<InvalidGuess> { Guess("") }
    }

    @Test
    fun `normalized trims and lowercases`() {
        assertEquals("hello", Guess("Hello").normalized())
    }
}
