package com.yonatankarp.beatthemachine.domain.riddle

import com.yonatankarp.beatthemachine.domain.Word

/**
 * Combines a word with its evaluation status.
 *
 * @property word The guessed word
 * @property status The evaluation status (CORRECT_POSITION, WRONG_POSITION, or WRONG_WORD)
 */
data class WordFeedback(
    val word: Word,
    val status: Status,
) {
    enum class Status {
        CORRECT_POSITION,
        WRONG_POSITION,
        WRONG_WORD,
    }
}
