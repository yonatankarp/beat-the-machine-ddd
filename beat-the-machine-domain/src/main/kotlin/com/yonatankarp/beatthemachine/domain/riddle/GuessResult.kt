package com.yonatankarp.beatthemachine.domain.riddle

/**
 * Represents the evaluation result of a guess.
 *
 * Simple value class wrapping a list of word feedbacks with no business logic.
 *
 * @property feedbacks List of feedback for each guessed word
 */
@JvmInline
value class GuessResult(
    val feedbacks: List<WordFeedback>,
)
