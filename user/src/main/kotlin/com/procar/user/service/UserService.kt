package com.procar.user.service

import com.procar.user.api.dto.BlockUserRequest
import com.procar.user.api.dto.CreateUserRequest
import com.procar.user.api.dto.UserDto
import com.procar.user.entity.UserEntity
import com.procar.user.repo.UserRepository
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val conversionService: ConversionService
) {

    private fun UserEntity.toDto(): UserDto =
        conversionService.convert(this, UserDto::class.java)!!

    suspend fun getUsers(): List<UserDto> {
        return userRepository.findAll().map { it.toDto() }
    }

    suspend fun createUser(request: CreateUserRequest): UserDto {
        val userEntity = UserEntity(username = request.username, roles = request.roles ?: listOf("USER"))
        return userRepository.save(userEntity).toDto()
    }

    suspend fun getUser(id: String): UserDto? {
        return userRepository.findById(id)?.toDto()
    }

    suspend fun blockUser(id: String, request: BlockUserRequest): UserDto? {
        val user = userRepository.findById(id) ?: return null
        val updatedUser = user.copy(blocked = request.blocked)
        return userRepository.save(updatedUser).toDto()
    }

    suspend fun getUserByUsername(username: String): UserDto? {
        return userRepository.findByUsername(username)?.toDto()
    }
}
