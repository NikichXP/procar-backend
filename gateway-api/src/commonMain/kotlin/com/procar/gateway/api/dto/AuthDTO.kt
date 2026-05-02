package com.procar.gateway.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class GatewayUserRegisterRequest(
    @ApiDoc(example = "john_doe") val username: String,
    @ApiDoc(example = "P@ssw0rd!") val password: String,
)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class AccessToken(val token: String, val validUntil: String)

@Serializable
data class AuthResult(
    val success: Boolean,
    val message: String? = null,
    val accessToken: AccessToken? = null,
    val refreshToken: String? = null,
)
