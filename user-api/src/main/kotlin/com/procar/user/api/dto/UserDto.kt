package com.procar.user.api.dto

data class UserDto(
    val id: String,
    val username: String,
    val blocked: Boolean,
    val roles: List<UserRole>,
    val brokerOrgId: String? = null
)
