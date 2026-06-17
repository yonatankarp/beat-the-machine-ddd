package com.yonatankarp.beatthemachine.web

import com.yonatankarp.beatthemachine.application.port.input.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.out.OptimisticLockConflict
import com.yonatankarp.beatthemachine.domain.ChallengeAlreadyOver
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(
    val message: String,
)

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(ChallengeNotFound::class)
    fun handleNotFound(ex: ChallengeNotFound): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse(ex.message ?: "Challenge not found"))

    @ExceptionHandler(ChallengeAlreadyOver::class)
    fun handleAlreadyOver(ex: ChallengeAlreadyOver): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse(ex.message ?: "Challenge is already over"))

    @ExceptionHandler(OptimisticLockConflict::class)
    fun handleOptimisticLock(ex: OptimisticLockConflict): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse(ex.message ?: "Concurrent modification conflict"))

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatusCode.valueOf(422)).body(ErrorResponse(ex.message ?: "Invalid input"))
}
