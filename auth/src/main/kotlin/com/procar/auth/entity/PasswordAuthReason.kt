package com.procar.auth.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document(collection = "password_auth")
data class PasswordAuthReason(
    @Id val id: ObjectId = ObjectId(),
    override val userId: String,
    val username: String,
    val salt: String,
    val passwordHash: String,
    @Field("last_login") val lastLogin: Instant? = null
) : AuthReason