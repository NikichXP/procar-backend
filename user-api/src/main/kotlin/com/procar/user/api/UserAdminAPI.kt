package com.procar.user.api

import com.procar.user.api.dto.*
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import org.springframework.web.service.annotation.DeleteExchange
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PatchExchange
import org.springframework.web.service.annotation.PostExchange

@HttpExchange("/api/admin/users")
interface UserAdminAPI {

    @GetExchange
    suspend fun getUsers(): List<UserDto>

    @GetExchange("/{id}")
    suspend fun getUser(@PathVariable id: String): UserDto

    @PostExchange
    suspend fun createUser(@Valid @RequestBody request: CreateUserRequest): UserDto

    @PostExchange("/{id}/status")
    suspend fun updateUserStatus(@PathVariable id: String, @Valid @RequestBody request: UpdateUserStatusRequest): UserDto

    @PatchExchange("/{id}")
    suspend fun patchUser(@PathVariable id: String, @Valid @RequestBody request: PatchUserRequest): UserDto

    @DeleteExchange("/{id}")
    suspend fun deleteUser(@PathVariable id: String)

    @Deprecated("Use patchUser instead")
    @PostExchange("/{id}/block")
    suspend fun blockUser(@PathVariable id: String, @Valid @RequestBody request: BlockUserRequest): UserDto

    @Deprecated("Use patchUser instead")
    @PostExchange("/{id}/roles")
    suspend fun updateUserRoles(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateUserRolesRequest
    ): UserDto

    @Deprecated("Use patchUser instead")
    @PostExchange("/{id}/broker")
    suspend fun updateUserBroker(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateUserBrokerRequest
    ): UserDto
}
