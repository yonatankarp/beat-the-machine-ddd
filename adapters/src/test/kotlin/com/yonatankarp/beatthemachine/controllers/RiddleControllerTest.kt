package com.yonatankarp.beatthemachine.controllers

import com.ninjasquad.springmockk.MockkBean
import com.yonatankarp.beatthemachine.models.GuessRequest
import com.yonatankarp.beatthemachine.models.GuessResponse
import com.yonatankarp.beatthemachine.models.GuessResponse.GuessResult
import com.yonatankarp.beatthemachine.models.Riddle
import com.yonatankarp.beatthemachine.services.RiddleManager
import com.yonatankarp.beatthemachine.services.RiddleService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [RiddleController::class])
class RiddleControllerTest {
    @MockkBean
    private lateinit var service: RiddleService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @ParameterizedTest
    @ValueSource(strings = ["/", "/index", "/index.html"])
    fun `should return index page`(endpoint: String) {
        val riddle = Riddle(1, "---", "cat", "some url")
        every { service.getRandomRiddle() } returns riddle

        mockMvc
            .get(endpoint)
            .andExpect {
                status { isOk() }
                view { name("index") }
                model {
                    attribute("guess", GuessResponse(emptyList()))
                    attribute("riddle", riddle)
                    attribute("response", listOf("---" to GuessResult.MISS))
                }
            }

        verify(exactly = 1) { service.getRandomRiddle() }
    }

    @Test
    fun `should return current riddle with full prompt solved`() {
        val riddleId = 3
        val riddle = Riddle(riddleId, "- --- -- --- ----", "a man on the moon", "some url")
        every { service.getRiddle(any()) } returns riddle

        mockMvc
            .post("/$riddleId/i-give-up")
            .andExpect {
                status { isOk() }
                view { name("game") }
                model {
                    attribute("guess", GuessResponse(emptyList()))
                    attribute("riddle", riddle)
                    attribute(
                        "response",
                        listOf(
                            "a" to GuessResult.MISS,
                            "man" to GuessResult.MISS,
                            "on" to GuessResult.MISS,
                            "the" to GuessResult.MISS,
                            "moon" to GuessResult.MISS,
                        ),
                    )
                }
            }

        verify(exactly = 1) { service.getRiddle(riddleId) }
    }

    @Test
    fun `should submit guess`() {
        val riddleId = 4
        val riddle = RiddleManager.riddles[riddleId]
        val guess = GuessRequest()
        val guessHits =
            listOf(
                "dragon" to GuessResult.MISS,
                "eating" to GuessResult.MISS,
                "a" to GuessResult.MISS,
                "cookie" to GuessResult.MISS,
            )
        every { service.handleGuess(any(), any()) } returns guessHits

        mockMvc
            .post("/$riddleId/guess") {
                sessionAttrs = mapOf("guess" to guess)
            }.andExpect {
                status { isOk() }
                view { name("game") }
                model {
                    attribute("guess", GuessRequest())
                    attribute("riddle", riddle)
                    attribute("response", guessHits)
                }
            }

        verify(exactly = 1) { service.handleGuess(eq(riddleId), eq(guess)) }
    }
}
