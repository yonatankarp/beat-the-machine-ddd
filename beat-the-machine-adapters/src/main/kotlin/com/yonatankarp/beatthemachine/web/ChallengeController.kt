package com.yonatankarp.beatthemachine.web

import com.yonatankarp.beatthemachine.application.port.input.ForfeitChallenge
import com.yonatankarp.beatthemachine.application.port.input.GetChallenge
import com.yonatankarp.beatthemachine.application.port.input.MakeGuess
import com.yonatankarp.beatthemachine.application.port.input.StartChallenge
import com.yonatankarp.beatthemachine.domain.ChallengeId
import com.yonatankarp.beatthemachine.domain.Difficulty
import com.yonatankarp.beatthemachine.domain.Guess
import com.yonatankarp.beatthemachine.web.dto.ChallengeResponse
import com.yonatankarp.beatthemachine.web.dto.GuessRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/challenges")
class ChallengeController(
    private val start: StartChallenge,
    private val makeGuess: MakeGuess,
    private val getChallenge: GetChallenge,
    private val forfeit: ForfeitChallenge,
) {
    @PostMapping
    fun startChallenge(
        @RequestParam(required = false) difficulty: Difficulty?,
    ): ResponseEntity<ChallengeResponse> {
        val challenge = start.start(difficulty ?: Difficulty.MEDIUM)
        return ResponseEntity.ok(ChallengeResponse.from(challenge))
    }

    @GetMapping("/{id}")
    fun getChallenge(
        @PathVariable id: String,
    ): ResponseEntity<ChallengeResponse> {
        val challengeId = ChallengeId(UUID.fromString(id))
        val challenge = getChallenge.get(challengeId)
        return ResponseEntity.ok(ChallengeResponse.from(challenge))
    }

    @PostMapping("/{id}/guesses")
    fun makeGuess(
        @PathVariable id: String,
        @RequestBody request: GuessRequest,
    ): ResponseEntity<ChallengeResponse> {
        val challengeId = ChallengeId(UUID.fromString(id))
        val (challenge, _) = makeGuess.guess(challengeId, Guess(request.word))
        return ResponseEntity.ok(ChallengeResponse.from(challenge))
    }

    @PostMapping("/{id}/forfeit")
    fun forfeit(
        @PathVariable id: String,
    ): ResponseEntity<ChallengeResponse> {
        val challengeId = ChallengeId(UUID.fromString(id))
        val challenge = forfeit.forfeit(challengeId)
        return ResponseEntity.ok(ChallengeResponse.from(challenge))
    }
}
