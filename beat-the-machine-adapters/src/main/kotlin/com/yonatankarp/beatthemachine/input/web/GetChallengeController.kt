package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.application.port.input.GetChallenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.input.web.dto.ChallengeResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/challenges")
class GetChallengeController(
    private val getChallenge: GetChallenge,
) {
    @GetMapping("/{id}")
    suspend fun get(
        @PathVariable id: UUID,
    ): ResponseEntity<ChallengeResponse> = ResponseEntity.ok(ChallengeResponse.from(getChallenge(ChallengeId(id))))
}
