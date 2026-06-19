package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.MaskedToken
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.openapi.v1.models.ChallengeResponse
import com.yonatankarp.beatthemachine.openapi.v1.models.ChallengeStatus
import com.yonatankarp.beatthemachine.openapi.v1.models.Difficulty
import com.yonatankarp.beatthemachine.openapi.v1.models.PictureStatus
import java.net.URI
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty as DomainDifficulty
import com.yonatankarp.beatthemachine.openapi.v1.models.MaskedToken as ApiMaskedToken
import com.yonatankarp.beatthemachine.openapi.v1.models.Picture as ApiPicture

fun Challenge.toApiResponse(): ChallengeResponse =
    ChallengeResponse(
        id = id.value,
        maskedPrompt = maskedPrompt().tokens.map { it.toApi() },
        livesRemaining = lives.remaining,
        maxLives = maxLives().remaining,
        status = ChallengeStatus.valueOf(status.name),
        picture = picture.toApi(),
    )

private fun MaskedToken.toApi(): ApiMaskedToken =
    when (this) {
        is MaskedToken.Revealed -> ApiMaskedToken(revealed = true, word = word, length = word.length)
        is MaskedToken.Hidden -> ApiMaskedToken(revealed = false, word = null, length = length)
    }

private fun Picture.toApi(): ApiPicture =
    when (this) {
        Picture.Pending -> {
            ApiPicture(PictureStatus.PENDING, null)
        }

        is Picture.Ready -> {
            runCatching { URI.create(url) }
                .map { ApiPicture(PictureStatus.READY, it) }
                .getOrDefault(ApiPicture(PictureStatus.FAILED, null))
        }

        Picture.Failed -> {
            ApiPicture(PictureStatus.FAILED, null)
        }
    }

fun Difficulty.toDomain(): DomainDifficulty = DomainDifficulty.valueOf(name)
