package com.procar.user.entity

import com.procar.commons.entity.VersionedEntity
import com.procar.user.api.dto.DepositStatus
import com.procar.user.api.dto.UserRole
import com.procar.user.api.dto.UserStatus
import com.procar.user.api.dto.VerificationStatus
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Document(collection = "users")
data class UserEntity(
    @Id
    @OptIn(ExperimentalUuidApi::class)
    val id: String = Uuid.generateV7().toString(),
    var username: String,
    var roles: List<UserRole>,
    var status: UserStatus = UserStatus.ACTIVE,
    var verificationStatus: VerificationStatus = VerificationStatus.NOT_STARTED,
    var depositStatus: DepositStatus = DepositStatus.NOT_PAID,
    var brokerId: String? = null,
    var companyName: String? = null,
    var country: String? = null
) : VersionedEntity()
