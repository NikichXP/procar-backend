package com.procar.user.api.dto

data class CreateUserRequest(val username: String, val roles: List<UserRole>? = null)
