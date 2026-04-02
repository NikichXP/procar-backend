package com.procar.core.api

import com.procar.core.service.admin.AdminUserConnectorService
import com.procar.user.api.dto.BlockUserRequest
import com.procar.user.api.dto.CreateUserRequest
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
}
