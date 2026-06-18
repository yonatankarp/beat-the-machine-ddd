package com.yonatankarp.beatthemachine.input.web

import com.ninjasquad.springmockk.MockkBean
import com.yonatankarp.beatthemachine.application.port.input.StartChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(StartChallengeController::class)
class StartChallengeControllerTest(
    @Autowired val mvc: MockMvc,
) {
    @MockkBean
    lateinit var startChallenge: StartChallenge

    @Test
    fun `POST creates a challenge and never leaks the prompt`() {
        every { startChallenge(any()) } returns Challenge.start(Prompt("hello world"), Lives(6))
        mvc.post("/api/challenges").andExpect {
            status { isOk() }
            jsonPath("$.livesRemaining") { value(6) }
            jsonPath("$.status") { value("IN_PROGRESS") }
            jsonPath("$.picture.status") { value("PENDING") }
            jsonPath("$.maskedPrompt[0].revealed") { value(false) }
            jsonPath("$.prompt") { doesNotExist() }
            jsonPath("$.secretPrompt") { doesNotExist() }
        }
    }

    @Test
    fun `invalid difficulty query param returns 422`() {
        mvc.post("/api/challenges?difficulty=NOPE").andExpect {
            status { isEqualTo(422) }
        }
    }
}
