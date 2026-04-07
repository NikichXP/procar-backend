package com.procar.user.entity

import com.procar.user.api.dto.UserRole
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Document(collection = "users")
data class UserEntity(
    @Id
    @OptIn(ExperimentalUuidApi::class)
    val id: String = Uuid.generateV7().toString(), // UUIDv7 for internal system ID
    val username: String,                                 // Primary user-facing identifier
    val publicId: String? = null,                         // Optional user-friendly ID (future use)
    val blocked: Boolean = false,
    val roles: List<UserRole> = listOf(UserRole.USER)             // User roles, defaults to USER
)
