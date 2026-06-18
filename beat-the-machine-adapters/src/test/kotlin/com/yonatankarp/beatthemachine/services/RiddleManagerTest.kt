package com.yonatankarp.beatthemachine.services

import com.yonatankarp.beatthemachine.models.Riddle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RiddleManagerTest {
    @Test
    fun `should map riddles`() {
        val actualFirstRiddle = RiddleManager.riddles[0]

        val expectedFirstRiddle =
            Riddle(
                id = 0,
                startPrompt = "--- ------ -- - ---",
                prompt = "man stands on a man",
                url = "https://s3.amazonaws.com/ai.protogenes/art/28b9da08-4282-11ed-8be2-ee31c059bf00.png",
            )

        assertEquals(expectedFirstRiddle, actualFirstRiddle)
    }
}
