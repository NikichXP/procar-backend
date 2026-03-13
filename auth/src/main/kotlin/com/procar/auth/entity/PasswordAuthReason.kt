package com.procar.auth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "password_auth")
data class PasswordAuthReason(
    @Id val id: String,
    override val userId: String,
    val salt: String,
    val passwordHash: String
): AuthReason