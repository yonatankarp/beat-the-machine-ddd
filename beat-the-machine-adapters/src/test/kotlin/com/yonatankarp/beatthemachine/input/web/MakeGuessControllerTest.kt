package com.yonatankarp.beatthemachine.input.web

import com.ninjasquad.springmockk.MockkBean
import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.MakeGuess
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.exception.ChallengeAlreadyOver
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Guess
import com.yonatankarp.beatthemachine.domain.valueobject.GuessOutcome
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(MakeGuessController::class)
class MakeGuessControllerTest(
    @Autowired val mvc: MockMvc,
) {
    @MockkBean
    lateinit var makeGuess: MakeGuess

    @Test
    fun `a successful guess returns the updated masked prompt`() {
        val (afterHit, _) = Challenge.start(Prompt("hello world"), Lives(6)).makeGuess(Guess("hello"))
        every { makeGuess(any(), any()) } returns (afterHit to GuessOutcome.HIT)
        mvc
            .post("/api/challenges/${ChallengeId.new().value}/guesses") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"word":"hello"}"""
            }.andExpect {
                status { isOk() }
                jsonPath("$.maskedPrompt[0].revealed") { value(true) }
                jsonPath("$.maskedPrompt[0].word") { value("hello") }
            }
    }

    @Test
    fun `guessing an unknown challenge returns 404`() {
        every { makeGuess(any(), any()) } throws ChallengeNotFound(ChallengeId.new())
        mvc
            .post("/api/challenges/${ChallengeId.new().value}/guesses") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"word":"hello"}"""
            }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `guessing on an already-over challenge returns 409`() {
        every { makeGuess(any(), any()) } throws ChallengeAlreadyOver(ChallengeId.new())
        mvc
            .post("/api/challenges/${ChallengeId.new().value}/guesses") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"word":"hello"}"""
            }.andExpect { status { isConflict() } }
    }

    @Test
    fun `guessing with a blank word returns 422`() {
        mvc
            .post("/api/challenges/${ChallengeId.new().value}/guesses") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"word":"   "}"""
            }.andExpect { status { isEqualTo(422) } }
    }
}
