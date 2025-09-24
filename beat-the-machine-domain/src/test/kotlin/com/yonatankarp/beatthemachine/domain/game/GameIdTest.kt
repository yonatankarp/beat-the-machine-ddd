package com.yonatankarp.beatthemachine.domain.game

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID

class GameIdTest {
    @Test
    fun `should create game id with uuid`() {
        // Given
        val uuid = UUID.randomUUID()

        // When
        val gameId = GameId(uuid)

        // Then
        gameId.value shouldBe uuid
    }
}
