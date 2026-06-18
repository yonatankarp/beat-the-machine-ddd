package com.yonatankarp.beatthemachine.domain.valueobject

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PromptTest {
    @Test
    fun `splits on single space`() {
        assertEquals(listOf("hello", "world"), Prompt("hello world").words())
    }

    @Test
    fun `splits on multiple whitespace characters`() {
        assertEquals(listOf("a", "b", "c"), Prompt("a\t b\n c").words())
    }

    @Test
    fun `rejects blank text`() {
        assertFailsWith<IllegalArgumentException> { Prompt("   ") }
    }

    @Test
    fun `rejects empty text`() {
        assertFailsWith<IllegalArgumentException> { Prompt("") }
    }
}
