package com.procar.user.controller

import com.procar.user.api.UserAPI
import com.procar.user.api.dto.UserInfoDto
import com.procar.user.service.UserService
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.bind.annotation.RestController
import kotlinx.coroutines.reactor.awaitSingle

@RestController
class UserAPIImpl(private val userService: UserService) : UserAPI {

    // TODO contents of this endpoint should go to separate service
    override suspend fun getCurrentUser(): UserInfoDto {
        // Get the current authenticated user from security context
        // TODO this should be replaced by something more elegant? or extracted to separate class like helper one
        val authentication = ReactiveSecurityContextHolder.getContext()
            .map { it.authentication }
            .awaitSingle()

        val username = authentication?.name
            ?: throw RuntimeException("User not authenticated")

        val user = userService.getUserByUsername(username)
            ?: throw RuntimeException("User not found")

        return UserInfoDto(
            id = user.id,
            username = user.username,
            roles = user.roles,
            status = user.status,
            verificationStatus = user.verificationStatus,
            depositStatus = user.depositStatus,
            brokerId = user.brokerId,
            companyName = user.companyName,
            country = user.country
        )
    }
}
