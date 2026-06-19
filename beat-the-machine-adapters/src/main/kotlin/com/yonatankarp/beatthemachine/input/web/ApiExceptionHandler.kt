package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.domain.exception.ChallengeAlreadyOver
import com.yonatankarp.beatthemachine.domain.exception.InvalidGuess
import com.yonatankarp.beatthemachine.openapi.v1.models.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebInputException

/**
 * Maps domain/application failures to HTTP status codes. Messages are fixed,
 * non-reflective strings: client input is never echoed back, so a malformed id or
 * a future invariant message can never leak through the error channel.
 */
@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(ChallengeNotFound::class)
    fun handleNotFound(ex: ChallengeNotFound): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse("challenge not found"))

    @ExceptionHandler(ChallengeAlreadyOver::class)
    fun handleAlreadyOver(ex: ChallengeAlreadyOver): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse("challenge is already over"))

    @ExceptionHandler(OptimisticLockConflict::class)
    fun handleOptimisticLock(ex: OptimisticLockConflict): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse("challenge was modified concurrently; retry the operation"))

    @ExceptionHandler(InvalidGuess::class)
    fun handleInvalidGuess(ex: InvalidGuess): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatusCode.valueOf(422)).body(ErrorResponse("invalid guess"))

    // Bean-validation failures on the request body (`@Valid`) surface in WebFlux as
    // WebExchangeBindException (the MVC MethodArgumentNotValidException equivalent).
    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidation(ex: WebExchangeBindException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatusCode.valueOf(422)).body(ErrorResponse("invalid request body"))

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatusCode.valueOf(422)).body(ErrorResponse("invalid input"))

    // Query-param conversion failures (e.g. a bad `difficulty` enum) and undeserializable
    // request bodies surface in WebFlux as ServerWebInputException (the MVC
    // MethodArgumentTypeMismatchException equivalent). WebExchangeBindException is a
    // subtype handled above, so it is matched first by Spring's most-specific resolution.
    @ExceptionHandler(ServerWebInputException::class)
    fun handleInputError(ex: ServerWebInputException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatusCode.valueOf(422)).body(ErrorResponse("invalid request parameter"))
}
