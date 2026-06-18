package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.application.port.input.MakeGuess
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Guess
import com.yonatankarp.beatthemachine.input.web.dto.ChallengeResponse
import com.yonatankarp.beatthemachine.input.web.dto.GuessRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/challenges")
class MakeGuessController(
    private val makeGuess: MakeGuess,
) {
    @PostMapping("/{id}/guesses")
    fun guess(
        @PathVariable id: UUID,
        @Valid @RequestBody request: GuessRequest,
    ): ResponseEntity<ChallengeResponse> {
        val (challenge, _) = makeGuess(ChallengeId(id), Guess(request.word))
        return ResponseEntity.ok(ChallengeResponse.from(challenge))
    }
}
