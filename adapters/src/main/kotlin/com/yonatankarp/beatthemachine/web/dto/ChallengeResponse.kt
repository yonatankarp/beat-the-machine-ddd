package com.yonatankarp.beatthemachine.web.dto

import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.MaskedToken
import com.yonatankarp.beatthemachine.domain.Picture

data class MaskedTokenDto(
    val revealed: Boolean,
    val word: String?,
)

data class PictureDto(
    val status: String,
    val url: String?,
)

data class ChallengeResponse(
    val id: String,
    val maskedPrompt: List<MaskedTokenDto>,
    val livesRemaining: Int,
    val status: String,
    val picture: PictureDto,
) {
    companion object {
        fun from(challenge: Challenge): ChallengeResponse =
            ChallengeResponse(
                id = challenge.id.value.toString(),
                maskedPrompt =
                    challenge.maskedPrompt().tokens.map { token ->
                        when (token) {
                            is MaskedToken.Revealed -> MaskedTokenDto(revealed = true, word = token.word)
                            MaskedToken.Hidden -> MaskedTokenDto(revealed = false, word = null)
                        }
                    },
                livesRemaining = challenge.lives.remaining,
                status = challenge.status.name,
                picture =
                    when (val pic = challenge.picture) {
                        Picture.Pending -> PictureDto(status = "PENDING", url = null)
                        is Picture.Ready -> PictureDto(status = "READY", url = pic.url)
                        Picture.Failed -> PictureDto(status = "FAILED", url = null)
                    },
            )
    }
}
