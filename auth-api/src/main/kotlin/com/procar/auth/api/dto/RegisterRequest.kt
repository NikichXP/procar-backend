package com.procar.auth.api.dto

data class RegisterRequest(
    val userId: String,
    val username: String,
    val password: String
)
