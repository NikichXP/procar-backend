package com.procar.gateway.api.dto

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole { CUSTOMER, BROKER, ADMIN }

@Serializable
enum class UserStatus { ACTIVE, BLOCKED }

@Serializable
enum class VerificationStatus { NOT_STARTED, PENDING, VERIFIED, REJECTED }

@Serializable
enum class DepositStatus { NOT_PAID, PENDING, ACTIVE, FORFEITED }

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val roles: List<UserRole>,
    val status: UserStatus = UserStatus.ACTIVE,
    val verificationStatus: VerificationStatus = VerificationStatus.NOT_STARTED,
    val depositStatus: DepositStatus = DepositStatus.NOT_PAID,
    val brokerId: String? = null,
    val companyName: String? = null,
    val country: String? = null,
)

@Serializable
data class CreateUserRequest(
    val username: String,
    val roles: List<UserRole>,
    val brokerId: String? = null,
    val companyName: String? = null,
    val country: String? = null,
)

@Serializable
data class PatchUserRequest(
    val username: String? = null,
    val roles: List<UserRole>? = null,
    val status: UserStatus? = null,
    val verificationStatus: VerificationStatus? = null,
    val depositStatus: DepositStatus? = null,
    val brokerId: String? = null,
    val companyName: String? = null,
    val country: String? = null,
)

@Serializable
data class UpdateUserStatusRequest(
    val status: UserStatus
)

@Serializable
data class UserInfoDto(
    val id: String,
    val username: String,
    val roles: List<UserRole>,
    val status: UserStatus,
    val verificationStatus: VerificationStatus,
    val depositStatus: DepositStatus,
    val brokerId: String? = null,
    val companyName: String? = null,
    val country: String? = null,
)
