package com.procar.core.service.admin

import com.procar.user.api.UserAdminAPI
import com.procar.user.api.dto.CreateUserRequest
import com.procar.user.api.dto.PatchUserRequest
import com.procar.user.api.dto.UpdateUserStatusRequest
import com.procar.user.api.dto.UserDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class AdminUserConnectorService(
    @Qualifier("userHttpClient") private val userController: UserAdminAPI
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

    suspend fun updateUserStatus(id: String, request: UpdateUserStatusRequest): UserDto {
        return userController.updateUserStatus(id, request)
    }

    suspend fun patchUser(id: String, request: PatchUserRequest): UserDto {
        return userController.patchUser(id, request)
    }

    suspend fun deleteUser(id: String) {
        userController.deleteUser(id)
    }
}
