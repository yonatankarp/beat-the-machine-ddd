package com.yonatankarp.beatthemachine.input.web

import com.ninjasquad.springmockk.MockkBean
import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.GetChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(GetChallengeController::class)
class GetChallengeControllerTest(
    @Autowired val mvc: MockMvc,
) {
    @MockkBean
    lateinit var getChallenge: GetChallenge

    @Test
    fun `GET returns the challenge state`() {
        val challenge = Challenge.start(Prompt("hello world"), Lives(6))
        every { getChallenge(any()) } returns challenge
        mvc.get("/api/challenges/${challenge.id.value}").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("IN_PROGRESS") }
            jsonPath("$.livesRemaining") { value(6) }
        }
    }

    @Test
    fun `GET an unknown challenge returns 404`() {
        every { getChallenge(any()) } throws ChallengeNotFound(ChallengeId.new())
        mvc.get("/api/challenges/${ChallengeId.new().value}").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `a malformed challenge id returns 422`() {
        mvc.get("/api/challenges/not-a-uuid").andExpect {
            status { isEqualTo(422) }
        }
    }
}
