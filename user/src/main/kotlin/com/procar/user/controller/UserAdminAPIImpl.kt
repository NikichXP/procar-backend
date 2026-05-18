package com.procar.user.controller

import com.procar.user.api.UserAdminAPI
import com.procar.user.api.dto.*
import com.procar.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class UserAdminAPIImpl(private val userService: UserService) : UserAdminAPI {

    override suspend fun getUsers(): List<UserDto> {
        return userService.getUsers()
    }

    override suspend fun getUser(@PathVariable id: String): UserDto {
        return userService.getUser(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    }

    override suspend fun createUser(@Valid @RequestBody request: CreateUserRequest): UserDto {
        return userService.createUser(request)
    }

    override suspend fun updateUserStatus(id: String, @Valid @RequestBody request: UpdateUserStatusRequest): UserDto {
        return userService.updateUserStatus(id, request.status)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    }

    override suspend fun patchUser(id: String, request: PatchUserRequest): UserDto {
        return userService.patchUser(id, request)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    }

    override suspend fun deleteUser(id: String) {
        userService.deleteUser(id)
    }

    override suspend fun blockUser(id: String, request: BlockUserRequest): UserDto {
        val status = if (request.blocked) UserStatus.BLOCKED else UserStatus.ACTIVE
        return userService.updateUserStatus(id, status)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    }

    override suspend fun updateUserRoles(id: String, request: UpdateUserRolesRequest): UserDto {
        return userService.patchUser(id, PatchUserRequest(roles = request.roles))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    }

    override suspend fun updateUserBroker(id: String, request: UpdateUserBrokerRequest): UserDto {
        return userService.patchUser(id, PatchUserRequest(brokerId = request.brokerOrgId))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    }
}
