package com.procar.user.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document(collection = "users")
data class UserEntity(
    @Id val id: String = UUID.randomUUID().toString(),
    val username: String,
    val blocked: Boolean = false
    // TODO: add balance, roles, etc. in future tasks
)
