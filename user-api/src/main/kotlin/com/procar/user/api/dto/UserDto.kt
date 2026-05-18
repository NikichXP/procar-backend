package com.procar.user.api.dto

data class UserDto(
    val id: String,
    val username: String,
    val roles: List<UserRole>,
    val status: UserStatus = UserStatus.ACTIVE,
    val verificationStatus: VerificationStatus = VerificationStatus.NOT_STARTED,
    val depositStatus: DepositStatus = DepositStatus.NOT_PAID,
    val brokerId: String? = null,
    val companyName: String? = null,
    val country: String? = null
)
