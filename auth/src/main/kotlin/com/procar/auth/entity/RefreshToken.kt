package com.procar.auth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "refresh_tokens")
data class RefreshToken(
    @Id
    val token: String,
    val userId: String,
    val createdAt: Instant = Instant.now()
)
