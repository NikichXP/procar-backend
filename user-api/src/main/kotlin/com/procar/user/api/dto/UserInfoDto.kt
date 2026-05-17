package com.procar.user.api.dto

data class UserInfoDto(
    val id: String,
    val username: String,
    val roles: List<UserRole>,
    val status: UserStatus,
    val verificationStatus: VerificationStatus,
    val depositStatus: DepositStatus,
    val brokerId: String? = null,
    val companyName: String? = null,
    val country: String? = null
)
