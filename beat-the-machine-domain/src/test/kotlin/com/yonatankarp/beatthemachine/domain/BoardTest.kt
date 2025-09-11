package com.yonatankarp.beatthemachine.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BoardTest {
    @Test
    fun `isSolved returns true when the board is solved`() {
        // Given
        val board = Board(
            shown = listOf("apple", "banana", "cherry")
        )

        // When
        val target = listOf(
            Word.of("apple"),
            Word.of("banana"),
            Word.of("cherry")
        )

        // Then
        assertTrue(board.isSolved(target))
    }

    @Test
    fun `isSolved returns false when the board is not solved`() {
        // Given
        val board = Board(
            shown = listOf("apple", "banana", "cherry")
        )

        // When
        val target = listOf(
            Word.of("apple"),
            Word.of("banana"),
            Word.of("date")
        )

        // Then
        assertFalse(board.isSolved(target))
    }

    @Test
    fun `isSolved is case insensitive`() {
        // Given
        val board = Board(
            shown = listOf("Apple", "BANANA", "chErry")
        )

        // When
        val target = listOf(
            Word.of("apple"),
            Word.of("banana"),
            Word.of("cherry")
        )

        // Then
        assertTrue(board.isSolved(target))
    }

    @Test
    fun `isSolved returns true when the board and target are both empty`() {
        // Given
        val board = Board(shown = emptyList())

        // When
        val target = emptyList<Word>()

        // Then
        assertTrue(board.isSolved(target))
    }

    @Test
    fun `isSolved returns false when the board and target have different sizes`() {
        // Given
        val board = Board(
            shown = listOf("apple", "banana")
        )

        // When
        val target = listOf(
            Word.of("apple"),
            Word.of("banana"),
            Word.of("cherry")
        )

        // Then
        assertFalse(board.isSolved(target))
    }
}
