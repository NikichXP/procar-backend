package com.procar.core.api.admin

import com.procar.core.service.UserService
import com.procar.core.service.admin.AdminUserConnectorService
import com.procar.user.api.dto.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/admin/users")
class AdminUserController(
    private val adminUserConnectorService: AdminUserConnectorService,
    private val userService: UserService
) {

    private suspend fun ensureNotSelf(id: String, action: String) {
        if (userService.getCurrentUserId() == id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot $action yourself")
        }
    }

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

    @PostMapping("/{id}/status")
    suspend fun updateUserStatus(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateUserStatusRequest
    ): UserDto {
        if (request.status == UserStatus.BLOCKED) {
            ensureNotSelf(id, "block")
        }
        return adminUserConnectorService.updateUserStatus(id, request)
    }

    @PatchMapping("/{id}")
    suspend fun patchUser(
        @PathVariable id: String,
        @Valid @RequestBody request: PatchUserRequest
    ): UserDto {
        if (request.status == UserStatus.BLOCKED) {
            ensureNotSelf(id, "block")
        }
        return adminUserConnectorService.patchUser(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteUser(@PathVariable id: String) {
        ensureNotSelf(id, "delete")
        adminUserConnectorService.deleteUser(id)
    }
}
