package com.procar.commons.error

import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebInputException

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> =
        build(HttpStatus.BAD_REQUEST, ex)

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ResponseEntity<ErrorResponse> =
        build(HttpStatus.CONFLICT, ex)

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<ErrorResponse> =
        build(HttpStatus.NOT_FOUND, ex)

    @ExceptionHandler(ServerWebInputException::class)
    fun handleBadInput(ex: ServerWebInputException): ResponseEntity<ErrorResponse> {
        val cause = ex.cause ?: ex
        return build(HttpStatus.valueOf(ex.statusCode.value()), cause, ex.reason ?: cause.message)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(ex: ResponseStatusException): ResponseEntity<ErrorResponse> {
        val cause = ex.cause ?: ex
        return build(HttpStatus.valueOf(ex.statusCode.value()), cause, ex.reason ?: cause.message)
    }

    @ExceptionHandler(WebClientResponseException::class)
    fun handleWebClientResponse(ex: WebClientResponseException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.valueOf(ex.statusCode.value())
        val reason = ex.responseBodyAsString.takeIf { it.isNotBlank() } ?: ex.message
        return build(status, ex, reason)
    }

    @ExceptionHandler(Exception::class)
    fun handleAny(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unhandled exception", ex)
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex)
    }

    private fun build(
        status: HttpStatus,
        ex: Throwable,
        reasonOverride: String? = null,
    ): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            type = ex::class.simpleName ?: ex::class.java.name,
            reason = reasonOverride ?: ex.message,
        )
        return ResponseEntity.status(status).body(body)
    }
}
