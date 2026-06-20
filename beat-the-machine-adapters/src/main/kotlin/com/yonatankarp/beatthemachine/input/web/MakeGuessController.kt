package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.application.port.input.MakeGuess
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Guess
import com.yonatankarp.beatthemachine.openapi.v1.MakeGuessApi
import com.yonatankarp.beatthemachine.openapi.v1.models.ChallengeResponse
import com.yonatankarp.beatthemachine.openapi.v1.models.GuessRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class MakeGuessController(
    private val makeGuessUseCase: MakeGuess,
) : MakeGuessApi {
    override suspend fun makeGuess(
        id: UUID,
        guessRequest: GuessRequest,
    ): ResponseEntity<ChallengeResponse> {
        val (challenge, _) = makeGuessUseCase handle MakeGuess.Command(ChallengeId(id), Guess(guessRequest.word))
        return ResponseEntity.ok(challenge.toApiResponse())
    }
}
