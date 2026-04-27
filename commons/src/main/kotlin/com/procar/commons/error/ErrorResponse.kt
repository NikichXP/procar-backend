package com.procar.commons.error

data class ErrorResponse(
    val status: String = "error",
    val type: String,
    val reason: String?,
)
