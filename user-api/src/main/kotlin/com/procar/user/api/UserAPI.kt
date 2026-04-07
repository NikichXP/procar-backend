package com.procar.user.api

import com.procar.user.api.dto.UserInfoDto
import org.springframework.web.bind.annotation.*
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange("/api")
interface UserAPI {

    @GetExchange("/me")
    suspend fun getCurrentUser(): UserInfoDto
}
