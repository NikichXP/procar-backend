package com.procar.user.converter

import com.procar.user.api.dto.UserDto
import com.procar.user.entity.UserEntity
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class UserEntityToUserDtoConverter : Converter<UserEntity, UserDto> {
    override fun convert(source: UserEntity): UserDto {
        return UserDto(
            id = source.id,
            username = source.username,
            roles = source.roles,
            status = source.status,
            verificationStatus = source.verificationStatus,
            depositStatus = source.depositStatus,
            brokerId = source.brokerId,
            companyName = source.companyName,
            country = source.country
        )
    }
}
