package com.procar.auth.api.dto

data class ChangePasswordRequest(
	val username: String,
	val oldPassword: String,
	val newPassword: String
)
