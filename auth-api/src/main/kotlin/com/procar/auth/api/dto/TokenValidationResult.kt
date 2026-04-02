package com.procar.auth.api.dto

import java.time.Instant

data class TokenValidationResult(
    val valid: Boolean,
    val userId: String?,
    val expiresAt: Instant? = null
)
