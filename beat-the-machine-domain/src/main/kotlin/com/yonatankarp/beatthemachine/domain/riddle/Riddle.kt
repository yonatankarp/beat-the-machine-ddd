package com.yonatankarp.beatthemachine.domain.riddle

/**
 * Rich domain entity containing the prompt and image URL with all evaluation business logic.
 *
 * Evaluates guesses against the original prompt and returns detailed positional feedback.
 *
 * @property prompt The original AI prompt that generated the image
 * @property imageUrl The URL of the AI-generated image
 */
data class Riddle(
    val prompt: Prompt,
    val imageUrl: ImageUrl,
) {
    fun evaluate(guess: Guess): GuessResult {
        val feedbacks =
            guess.words.mapIndexed { index, word ->
                val status = determineWordStatus(word, index)
                WordFeedback(word, status)
            }
        return GuessResult(feedbacks)
    }

    private fun determineWordStatus(
        word: Word,
        index: Int,
    ): WordFeedback.Status =
        when {
            isCorrectPosition(word, index) -> WordFeedback.Status.CORRECT_POSITION
            isWrongPosition(word) -> WordFeedback.Status.WRONG_POSITION
            else -> WordFeedback.Status.WRONG_WORD
        }

    private fun isCorrectPosition(
        word: Word,
        index: Int,
    ) = index < prompt.words.size && word == prompt.words[index]

    private fun isWrongPosition(word: Word) = prompt contains word
}
