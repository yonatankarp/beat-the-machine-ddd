package com.yonatankarp.beatthemachine.web

import com.ninjasquad.springmockk.MockkBean
import com.yonatankarp.beatthemachine.application.port.input.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.ForfeitChallenge
import com.yonatankarp.beatthemachine.application.port.input.GetChallenge
import com.yonatankarp.beatthemachine.application.port.input.MakeGuess
import com.yonatankarp.beatthemachine.application.port.input.StartChallenge
import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.ChallengeId
import com.yonatankarp.beatthemachine.domain.Lives
import com.yonatankarp.beatthemachine.domain.Prompt
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(ChallengeController::class)
class ChallengeControllerTest(
    @Autowired val mvc: MockMvc,
) {
    @MockkBean
    lateinit var start: StartChallenge

    @MockkBean
    lateinit var makeGuess: MakeGuess

    @MockkBean
    lateinit var getChallenge: GetChallenge

    @MockkBean
    lateinit var forfeit: ForfeitChallenge

    @Test
    fun `POST creates a challenge and never leaks the prompt`() {
        every { start.start(any()) } returns Challenge.start(Prompt("hello world"), Lives(6))
        mvc.post("/api/challenges").andExpect {
            status { isOk() }
            jsonPath("$.livesRemaining") { value(6) }
            jsonPath("$.status") { value("IN_PROGRESS") }
            jsonPath("$.picture.status") { value("PENDING") }
            jsonPath("$.maskedPrompt[0].revealed") { value(false) }
        }
    }

    @Test
    fun `guessing an unknown challenge returns 404`() {
        every { makeGuess.guess(any(), any()) } throws ChallengeNotFound(ChallengeId.new())
        mvc
            .post("/api/challenges/${ChallengeId.new().value}/guesses") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"word":"hello"}"""
            }.andExpect { status { isNotFound() } }
    }
}
