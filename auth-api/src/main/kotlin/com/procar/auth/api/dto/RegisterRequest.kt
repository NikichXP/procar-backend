package com.procar.auth.api.dto

data class RegisterRequest(
    val userId: String,
    val login: String,
    val password: String
)
