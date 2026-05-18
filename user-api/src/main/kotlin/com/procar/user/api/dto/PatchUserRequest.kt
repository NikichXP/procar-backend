package com.procar.user.api.dto

data class PatchUserRequest(
    val username: String? = null,
    val roles: List<UserRole>? = null,
    val brokerId: String? = null,
    val companyName: String? = null,
    val country: String? = null,
    val status: UserStatus? = null,
    val verificationStatus: VerificationStatus? = null,
    val depositStatus: DepositStatus? = null
)
