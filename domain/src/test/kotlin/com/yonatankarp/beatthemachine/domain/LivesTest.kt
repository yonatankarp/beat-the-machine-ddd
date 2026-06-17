package com.yonatankarp.beatthemachine.domain

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
}
