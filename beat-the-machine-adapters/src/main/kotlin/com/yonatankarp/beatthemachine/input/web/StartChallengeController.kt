package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.application.port.input.StartChallenge
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.input.web.dto.ChallengeResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/challenges")
class StartChallengeController(
    private val startChallenge: StartChallenge,
) {
    @PostMapping
    fun start(
        @RequestParam(required = false) difficulty: Difficulty?,
    ): ResponseEntity<ChallengeResponse> = ResponseEntity.ok(ChallengeResponse.from(startChallenge(difficulty ?: Difficulty.MEDIUM)))
}
