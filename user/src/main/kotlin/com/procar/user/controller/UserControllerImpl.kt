package com.procar.user.controller

import com.procar.user.api.dto.BlockUserRequest
import com.procar.user.api.dto.CreateUserRequest
import com.procar.user.api.dto.UserDto
import com.procar.user.api.UserController
import com.procar.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class UserControllerImpl(private val userService: UserService) : UserController {

    override suspend fun getUser(@PathVariable id: String): UserDto {
        return userService.getUser(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    }

    override suspend fun createUser(@Valid @RequestBody request: CreateUserRequest): UserDto {
        return userService.createUser(request)
    }

    override suspend fun blockUser(
        @PathVariable id: String,
        @Valid @RequestBody request: BlockUserRequest
    ): UserDto {
        return userService.blockUser(id, request)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    }
}
