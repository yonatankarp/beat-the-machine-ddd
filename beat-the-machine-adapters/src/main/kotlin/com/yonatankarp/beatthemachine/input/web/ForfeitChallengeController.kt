package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.application.port.input.ForfeitChallenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.input.web.dto.ChallengeResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/challenges")
class ForfeitChallengeController(
    private val forfeitChallenge: ForfeitChallenge,
) {
    @PostMapping("/{id}/forfeit")
    fun forfeit(
        @PathVariable id: UUID,
    ): ResponseEntity<ChallengeResponse> = ResponseEntity.ok(ChallengeResponse.from(forfeitChallenge(ChallengeId(id))))
}
