package com.procar.gateway.api.dto

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole { USER, BROKER, ADMIN }

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val blocked: Boolean,
    val roles: List<UserRole>,
    val brokerOrgId: String? = null,
)

@Serializable
data class CreateUserRequest(
    val username: String,
    val brokerOrgId: String? = null,
    val roles: List<UserRole>? = null,
)

@Serializable
data class BlockUserRequest(val blocked: Boolean)

@Serializable
data class UpdateUserRolesRequest(val roles: List<UserRole>)

@Serializable
data class UpdateUserBrokerRequest(val brokerOrgId: String? = null)
