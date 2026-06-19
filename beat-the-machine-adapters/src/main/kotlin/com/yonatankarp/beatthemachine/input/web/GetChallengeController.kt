package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.application.port.input.GetChallenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.openapi.v1.GetChallengeApi
import com.yonatankarp.beatthemachine.openapi.v1.models.ChallengeResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class GetChallengeController(
    private val getChallengeUseCase: GetChallenge,
) : GetChallengeApi {
    override suspend fun getChallenge(id: UUID): ResponseEntity<ChallengeResponse> =
        ResponseEntity.ok(getChallengeUseCase(ChallengeId(id)).toApiResponse())
}
