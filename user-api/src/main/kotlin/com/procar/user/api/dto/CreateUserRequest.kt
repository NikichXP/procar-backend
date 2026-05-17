package com.procar.user.api.dto

data class CreateUserRequest(
    val username: String,
    val roles: List<UserRole>,
    val brokerId: String? = null,
    val companyName: String? = null,
    val country: String? = null
)
