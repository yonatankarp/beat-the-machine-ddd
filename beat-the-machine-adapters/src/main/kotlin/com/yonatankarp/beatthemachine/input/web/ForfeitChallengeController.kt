package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.application.port.input.ForfeitChallenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.openapi.v1.ForfeitChallengeApi
import com.yonatankarp.beatthemachine.openapi.v1.models.ChallengeResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class ForfeitChallengeController(
    private val forfeitChallengeUseCase: ForfeitChallenge,
) : ForfeitChallengeApi {
    override suspend fun forfeitChallenge(id: UUID): ResponseEntity<ChallengeResponse> =
        ResponseEntity.ok(
            (forfeitChallengeUseCase handle ForfeitChallenge.Command(ChallengeId(id))).toApiResponse(),
        )
}
