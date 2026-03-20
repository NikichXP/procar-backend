package com.procar.auth.api.dto

data class TokenValidationResult(
    val valid: Boolean,
    val userId: String?
)
