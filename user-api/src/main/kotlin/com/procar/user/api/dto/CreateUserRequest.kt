package com.procar.user.api.dto

data class CreateUserRequest(
    val username: String,
    val brokerOrgId: String? = null,
    val roles: List<UserRole>? = null
)
