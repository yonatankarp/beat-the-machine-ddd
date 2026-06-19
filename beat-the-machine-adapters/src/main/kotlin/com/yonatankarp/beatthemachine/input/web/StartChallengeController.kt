package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.application.port.input.StartChallenge
import com.yonatankarp.beatthemachine.openapi.v1.StartChallengeApi
import com.yonatankarp.beatthemachine.openapi.v1.models.ChallengeResponse
import com.yonatankarp.beatthemachine.openapi.v1.models.Difficulty
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty as DomainDifficulty

@RestController
class StartChallengeController(
    private val startChallengeUseCase: StartChallenge,
) : StartChallengeApi {
    override suspend fun startChallenge(difficulty: Difficulty?): ResponseEntity<ChallengeResponse> =
        ResponseEntity.ok(startChallengeUseCase(difficulty?.toDomain() ?: DomainDifficulty.MEDIUM).toApiResponse())
}
