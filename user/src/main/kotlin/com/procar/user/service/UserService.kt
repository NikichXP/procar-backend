package com.procar.user.service

import com.procar.user.api.dto.BlockUserRequest
import com.procar.user.api.dto.CreateUserRequest
import com.procar.user.api.dto.UserDto
import com.procar.user.entity.UserEntity
import com.procar.user.repo.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

    suspend fun getUsers(): List<UserDto> {
        return userRepository.findAll()
            .map { UserDto(id = it.id, username = it.username, blocked = it.blocked) }
    }

    suspend fun createUser(request: CreateUserRequest): UserDto {
        val userEntity = UserEntity(username = request.username)
        val savedUser = userRepository.save(userEntity)
        return UserDto(id = savedUser.id, username = savedUser.username, blocked = savedUser.blocked)
    }

    suspend fun getUser(id: String): UserDto? {
        val user = userRepository.findById(id)
        return user?.let { UserDto(id = it.id, username = it.username, blocked = it.blocked) }
    }

    suspend fun blockUser(id: String, request: BlockUserRequest): UserDto? {
        val user = userRepository.findById(id)
        return if (user != null) {
            val updatedUser = user.copy(blocked = request.blocked)
            val savedUser = userRepository.save(updatedUser)
            UserDto(id = savedUser.id, username = savedUser.username, blocked = savedUser.blocked)
        } else {
            null
        }
    }
}
