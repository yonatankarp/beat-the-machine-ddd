package com.yonatankarp.beatthemachine.input.web.dto

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ChallengeResponseTest {
    private fun challenge() = Challenge.start(Prompt("hello world"), Lives(6))

    @Test
    fun `maps a pending picture`() {
        val response = ChallengeResponse.from(challenge())
        assertEquals(PictureDto("PENDING", null), response.picture)
    }

    @Test
    fun `maps a ready picture with its url`() {
        val response = ChallengeResponse.from(challenge().withPicture(Picture.Ready("http://img/1.png")))
        assertEquals(PictureDto("READY", "http://img/1.png"), response.picture)
    }

    @Test
    fun `maps a failed picture`() {
        val response = ChallengeResponse.from(challenge().withPicture(Picture.Failed))
        assertEquals(PictureDto("FAILED", null), response.picture)
    }

    @Test
    fun `hides every word while the challenge is in progress`() {
        val response = ChallengeResponse.from(challenge())
        assertEquals(listOf(MaskedTokenDto(false, null), MaskedTokenDto(false, null)), response.maskedPrompt)
    }

    @Test
    fun `reveals the whole prompt once the challenge is lost`() {
        val response = ChallengeResponse.from(challenge().forfeit())
        assertEquals(
            listOf(MaskedTokenDto(true, "hello"), MaskedTokenDto(true, "world")),
            response.maskedPrompt,
        )
        assertEquals("LOST", response.status)
    }
}
