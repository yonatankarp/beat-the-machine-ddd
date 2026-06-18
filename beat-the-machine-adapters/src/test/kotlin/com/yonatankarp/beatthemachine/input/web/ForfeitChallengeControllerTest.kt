package com.yonatankarp.beatthemachine.input.web

import com.ninjasquad.springmockk.MockkBean
import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.input.ForfeitChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(ForfeitChallengeController::class)
class ForfeitChallengeControllerTest(
    @Autowired val mvc: MockMvc,
) {
    @MockkBean
    lateinit var forfeitChallenge: ForfeitChallenge

    @Test
    fun `forfeit reveals the prompt and reports LOST`() {
        every { forfeitChallenge(any()) } returns Challenge.start(Prompt("hello world"), Lives(6)).forfeit()
        mvc.post("/api/challenges/${ChallengeId.new().value}/forfeit").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("LOST") }
            jsonPath("$.maskedPrompt[0].revealed") { value(true) }
        }
    }

    @Test
    fun `forfeit with concurrent modification returns 409`() {
        every { forfeitChallenge(any()) } throws OptimisticLockConflict(ChallengeId.new())
        mvc
            .post("/api/challenges/${ChallengeId.new().value}/forfeit")
            .andExpect { status { isConflict() } }
    }
}
