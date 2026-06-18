package com.yonatankarp.beatthemachine.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GuessTest {
    @Test
    fun `rejects blank word`() {
        assertFailsWith<IllegalArgumentException> { Guess("   ") }
    }

    @Test
    fun `rejects empty word`() {
        assertFailsWith<IllegalArgumentException> { Guess("") }
    }

    @Test
    fun `normalized trims and lowercases`() {
        assertEquals("hello", Guess("Hello").normalized())
    }
}
