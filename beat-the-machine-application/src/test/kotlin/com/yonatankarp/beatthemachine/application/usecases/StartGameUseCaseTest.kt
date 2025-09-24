package com.yonatankarp.beatthemachine.application.usecases

import com.yonatankarp.beatthemachine.application.ports.output.FindAvailableRiddles
import com.yonatankarp.beatthemachine.domain.riddle.ImageUrl
import com.yonatankarp.beatthemachine.domain.riddle.Prompt
import com.yonatankarp.beatthemachine.domain.riddle.Riddle
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class StartGameUseCaseTest {
    @Test
    fun `should start game with available riddles from repository`() {
        // Given
        val availableRiddles =
            listOf(
                Riddle(
                    prompt = Prompt.of("sunset", "beach"),
                    imageUrl = ImageUrl("https://example.com/image1.jpg"),
                ),
                Riddle(
                    prompt = Prompt.of("mountain", "landscape"),
                    imageUrl = ImageUrl("https://example.com/image2.jpg"),
                ),
            )
        val findAvailableRiddles = FindAvailableRiddles { availableRiddles }
        val startGameUseCase = StartGame(findAvailableRiddles)

        // When
        val game = startGameUseCase()

        // Then
        game shouldNotBe null
        game.riddles shouldContainExactly availableRiddles
    }
}
