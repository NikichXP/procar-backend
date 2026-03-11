package com.procar.auth.entity

data class PasswordAuthReason(
    val id: String,
    override val userId: String,
    val salt: String,
    val passwordHash: String
): AuthReason(userId)