package com.procar.core.converter

import com.procar.gateway.api.dto.*
import com.procar.user.api.dto.UserDto
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class UserDtoToUserInfoDtoConverter : Converter<UserDto, UserInfoDto> {
    override fun convert(source: UserDto): UserInfoDto = UserInfoDto(
        id = source.id,
        username = source.username,
        roles = source.roles.map { UserRole.valueOf(it.name) },
        status = UserStatus.valueOf(source.status.name),
        verificationStatus = VerificationStatus.valueOf(source.verificationStatus.name),
        depositStatus = DepositStatus.valueOf(source.depositStatus.name),
        brokerId = source.brokerId,
        companyName = source.companyName,
        country = source.country
    )
}
