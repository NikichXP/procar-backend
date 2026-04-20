package com.procar.core.api.admin

import com.procar.core.service.admin.AdminUserConnectorService
import com.procar.user.api.dto.BlockUserRequest
import com.procar.user.api.dto.CreateUserRequest
import com.procar.user.api.dto.UpdateUserBrokerRequest
import com.procar.user.api.dto.UpdateUserRolesRequest
import com.procar.user.api.dto.UserDto
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/users")
class AdminUserController(
    private val adminUserConnectorService: AdminUserConnectorService
) {

    @GetMapping
    suspend fun getUsers(): List<UserDto> {
        return adminUserConnectorService.getUsers()
    }

    @GetMapping("/{id}")
    suspend fun getUser(@PathVariable id: String): UserDto {
        return adminUserConnectorService.getUser(id)
    }

    @PostMapping
    suspend fun createUser(@Valid @RequestBody request: CreateUserRequest): UserDto {
        return adminUserConnectorService.createUser(request)
    }

    @PostMapping("/{id}/block")
    suspend fun blockUser(
        @PathVariable id: String,
        @Valid @RequestBody request: BlockUserRequest
    ): UserDto {
        return adminUserConnectorService.blockUser(id, request)
    }

    @PostMapping("/{id}/roles")
    suspend fun updateUserRoles(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateUserRolesRequest
    ): UserDto {
        return adminUserConnectorService.updateUserRoles(id, request)
    }

    @PostMapping("/{id}/broker")
    suspend fun updateUserBroker(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateUserBrokerRequest
    ): UserDto {
        return adminUserConnectorService.updateUserBroker(id, request)
    }
}
