package com.yonatankarp.beatthemachine.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringUtilsTest {
    @Test
    fun `should map to hidden string`() {
        // Given
        val input = "I am a secret string"

        // When
        val actual = input.toHiddenString()

        // Then
        val expected = "- -- - ------ ------"
        assertEquals(expected, actual)
    }
}
