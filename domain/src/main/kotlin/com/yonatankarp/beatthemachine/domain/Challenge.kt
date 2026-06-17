package com.yonatankarp.beatthemachine.domain

class Challenge private constructor(
    val id: ChallengeId,
    private val prompt: Prompt,
    guesses: Set<Guess>,
    lives: Lives,
    status: ChallengeStatus,
    picture: Picture,
    val difficulty: Difficulty,
    val version: Long,
) {
    private val _guesses = guesses.toMutableSet()
    val guesses: Set<Guess> get() = _guesses.toSet()
    var lives: Lives = lives
        private set
    var status: ChallengeStatus = status
        private set
    var picture: Picture = picture
        private set

    fun maskedPrompt(): MaskedPrompt =
        if (status == ChallengeStatus.LOST) {
            MaskedPrompt.of(prompt, prompt.allWordsAsGuesses())
        } else {
            MaskedPrompt.of(prompt, _guesses)
        }

    fun makeGuess(guess: Guess): GuessOutcome {
        if (status != ChallengeStatus.IN_PROGRESS) throw ChallengeAlreadyOver(id)
        val normalized = guess.normalized()
        if (_guesses.any { it.normalized() == normalized }) return GuessOutcome.DUPLICATE
        _guesses.add(guess)
        val hit = prompt.words().any { it.lowercase() == normalized }
        return if (hit) {
            if (MaskedPrompt.of(prompt, _guesses).isFullyRevealed()) status = ChallengeStatus.BEATEN
            GuessOutcome.HIT
        } else {
            lives = lives.lose()
            if (lives.isExhausted()) status = ChallengeStatus.LOST
            GuessOutcome.MISS
        }
    }

    fun forfeit() {
        if (status != ChallengeStatus.IN_PROGRESS) throw ChallengeAlreadyOver(id)
        status = ChallengeStatus.LOST
    }

    fun withPicture(picture: Picture): Challenge =
        rehydrate(
            id,
            prompt,
            guesses,
            lives,
            status,
            picture,
            difficulty,
            version = version + 1,
        )

    // Exposed only to persistence mapping in the adapter module.
    fun secretPrompt(): Prompt = prompt

    companion object {
        fun start(
            prompt: Prompt,
            lives: Lives,
            picture: Picture = Picture.Pending,
            difficulty: Difficulty = Difficulty.MEDIUM,
        ): Challenge =
            Challenge(
                ChallengeId.new(),
                prompt,
                emptySet(),
                lives,
                ChallengeStatus.IN_PROGRESS,
                picture,
                difficulty,
                version = 0,
            )

        fun rehydrate(
            id: ChallengeId,
            prompt: Prompt,
            guesses: Set<Guess>,
            lives: Lives,
            status: ChallengeStatus,
            picture: Picture,
            difficulty: Difficulty,
            version: Long,
        ): Challenge = Challenge(id, prompt, guesses, lives, status, picture, difficulty, version)
    }
}

private fun Prompt.allWordsAsGuesses(): Set<Guess> = words().map { Guess(it) }.toSet()
