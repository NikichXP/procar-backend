package com.procar.user.api.dto

data class UserInfoDto(
    val id: String,
    val username: String,
    val roles: List<UserRole>
)
