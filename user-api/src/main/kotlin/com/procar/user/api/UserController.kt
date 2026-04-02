package com.procar.user.api

import com.procar.user.api.dto.BlockUserRequest
import com.procar.user.api.dto.CreateUserRequest
import com.procar.user.api.dto.UserDto
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

@HttpExchange("/api")
interface UserController {

    @GetExchange("/users/{id}")
    fun getUser(@PathVariable id: String): UserDto

    @PostExchange("/users")
    fun createUser(@Valid @RequestBody request: CreateUserRequest): UserDto

    @PostExchange("/users/{id}/block")
    fun blockUser(@PathVariable id: String, @Valid @RequestBody request: BlockUserRequest): UserDto
}
