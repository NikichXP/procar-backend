package com.procar.auth.dto

data class RegisterRequest(
    val userId: String,
    val username: String,
    val password: String
)
