package com.procar.auth.api.dto

data class AuthResult(
	val success: Boolean,
	val message: String? = null,
	val accessToken: AccessToken? = null,
	val refreshToken: String? = null
)
