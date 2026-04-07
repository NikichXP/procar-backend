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
            blocked = source.blocked,
            roles = source.roles
        )
    }
}
