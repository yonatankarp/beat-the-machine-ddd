package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import com.yonatankarp.beatthemachine.openapi.v1.models.MaskedToken
import com.yonatankarp.beatthemachine.openapi.v1.models.PictureStatus
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.test.assertEquals

class ChallengeApiMapperTest {
    private fun challenge() = Challenge.start(Prompt("hello world"), Lives(6))

    @Test
    fun `maps a pending picture`() {
        val r = challenge().toApiResponse()
        assertEquals(PictureStatus.PENDING, r.picture.status)
        assertEquals(null, r.picture.url)
    }

    @Test
    fun `maps a ready picture with its url`() {
        val r = challenge().withPicture(Picture.Ready("http://img/1.png")).toApiResponse()
        assertEquals(PictureStatus.READY, r.picture.status)
        assertEquals(URI.create("http://img/1.png"), r.picture.url)
    }

    @Test
    fun `maps a failed picture`() {
        val r = challenge().withPicture(Picture.Failed).toApiResponse()
        assertEquals(PictureStatus.FAILED, r.picture.status)
        assertEquals(null, r.picture.url)
    }

    @Test
    fun `hides every word while the challenge is in progress`() {
        val r = challenge().toApiResponse()
        assertEquals(listOf(MaskedToken(false, null), MaskedToken(false, null)), r.maskedPrompt)
    }

    @Test
    fun `reveals the whole prompt once the challenge is lost`() {
        val r = challenge().forfeit().toApiResponse()
        assertEquals(listOf(MaskedToken(true, "hello"), MaskedToken(true, "world")), r.maskedPrompt)
        assertEquals("LOST", r.status.value)
    }
}
