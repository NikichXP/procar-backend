package com.procar.core

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException

@SpringBootApplication
class CoreApplication

fun main(args: Array<String>) {
	runApplication<CoreApplication>(*args)
}

@ControllerAdvice
class GlobalExceptionHandler {
	
	@ExceptionHandler(WebClientResponseException::class)
	fun handleWebClientResponseException(ex: WebClientResponseException): ResponseStatusException {
		return ResponseStatusException(
			HttpStatus.valueOf(ex.statusCode.value()),
			ex.responseBodyAsString ?: ex.message
		)
	}
}
