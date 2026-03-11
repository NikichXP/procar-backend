package com.procar.auth.dto

import java.time.Instant

data class AccessToken(
	val token: String,
	val validUntil: Instant
)
