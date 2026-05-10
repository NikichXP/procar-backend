package com.procar.commons.error

import org.springframework.http.HttpStatus

interface ExceptionLogger {
    fun log(status: HttpStatus, ex: Throwable)
}
