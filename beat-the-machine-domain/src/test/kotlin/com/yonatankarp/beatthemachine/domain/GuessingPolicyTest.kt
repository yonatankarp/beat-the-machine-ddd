package com.yonatankarp.beatthemachine.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GuessingPolicyTest {
    @Test
    fun `initial mask works as expected`() {
        // Given
        val policy = GuessingPolicy()
        val target = listOf("apple", "banana", "cherry").map(Word::of)

        // When
        val initialMask = policy.initialMask(target)

        // Then
        val expected = listOf("-----", "------", "------")
        assertEquals(expected, initialMask)
    }

    @Test
    fun `reveal works as expected`() {
        // Given
        val policy = GuessingPolicy()
        val target = listOf("apple", "banana", "cherry").map(Word::of)
        val current = listOf("-----", "------", "------")
        val guess = listOf("banana", "date").map(Word::of)

        // When
        val revealed = policy.reveal(target, current, guess)

        // Then
        val expected =
            listOf(
                "-----" to "-----",
                "------" to "banana",
                "------" to "------",
            )
        assertEquals(expected, revealed)
    }

    @Test
    fun `revealAll works as expected`() {
        // Given
        val policy = GuessingPolicy()
        val target = listOf("apple", "banana", "cherry").map(Word::of)

        // When
        val revealed = policy.revealAll(target)

        // Then
        val expected = listOf("apple", "banana", "cherry")
        assertEquals(expected, revealed)
    }
}
