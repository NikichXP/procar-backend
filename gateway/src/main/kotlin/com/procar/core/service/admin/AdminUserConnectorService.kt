package com.procar.core.service.admin

import com.procar.user.api.UserController
import com.procar.user.api.dto.BlockUserRequest
import com.procar.user.api.dto.CreateUserRequest
import com.procar.user.api.dto.UserDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class AdminUserConnectorService(
    @Qualifier("userHttpClient") private val userController: UserController
) {

    suspend fun getUsers(): List<UserDto> {
        return userController.getUsers()
    }

    suspend fun createUser(request: CreateUserRequest): UserDto {
        return userController.createUser(request)
    }

    suspend fun getUser(id: String): UserDto {
        return userController.getUser(id)
    }

    suspend fun blockUser(id: String, request: BlockUserRequest): UserDto {
        return userController.blockUser(id, request)
    }
}
