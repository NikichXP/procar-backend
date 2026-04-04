package com.procar.user.api

import com.procar.user.api.dto.BlockUserRequest
import com.procar.user.api.dto.CreateUserRequest
import com.procar.user.api.dto.UserDto
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

@HttpExchange("/api/admin/users")
interface UserAdminAPI {

    @GetExchange
    suspend fun getUsers(): List<UserDto>

    @GetExchange("/{id}")
    suspend fun getUser(@PathVariable id: String): UserDto

    @PostExchange
    suspend fun createUser(@Valid @RequestBody request: CreateUserRequest): UserDto

    @PostExchange("/{id}/block")
    suspend fun blockUser(@PathVariable id: String, @Valid @RequestBody request: BlockUserRequest): UserDto
}
