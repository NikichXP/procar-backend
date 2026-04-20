package com.procar.core.service.admin

import com.procar.user.api.UserAdminAPI
import com.procar.user.api.dto.BlockUserRequest
import com.procar.user.api.dto.CreateUserRequest
import com.procar.user.api.dto.UpdateUserBrokerRequest
import com.procar.user.api.dto.UpdateUserRolesRequest
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

    suspend fun blockUser(id: String, request: BlockUserRequest): UserDto {
        return userController.blockUser(id, request)
    }

    suspend fun updateUserRoles(id: String, request: UpdateUserRolesRequest): UserDto {
        return userController.updateUserRoles(id, request)
    }

    suspend fun updateUserBroker(id: String, request: UpdateUserBrokerRequest): UserDto {
        return userController.updateUserBroker(id, request)
    }
}
