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
    val id: String = Uuid.generateV7().toString(),
    val username: String,
    val publicId: String? = null,
    val brokerOrgId: String? = null,
    val blocked: Boolean = false,
    val roles: List<UserRole> = listOf(UserRole.USER)
)
