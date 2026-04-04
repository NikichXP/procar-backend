package com.procar.core.service

import com.procar.core.api.dto.BidStatus
import com.procar.core.api.dto.LotSummary
import com.procar.core.api.dto.UserBidPage
import com.procar.core.service.admin.AdminUserConnectorService
import com.procar.user.api.dto.UserInfoDto
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val adminUserConnectorService: AdminUserConnectorService
) {

    suspend fun getCurrentUsername(): String {
        // Get the current authenticated user from security context
        val authentication = ReactiveSecurityContextHolder.getContext()
            .map { it.authentication }
            .awaitFirst()

        return authentication?.name
            ?: throw RuntimeException("User not authenticated")
    }

    fun getUserInfo(username: String): UserInfoDto {
        // Get user by username from the user service
        val users = runBlocking { adminUserConnectorService.getUsers() }
        val user = users.find { it.username == username }
            ?: throw RuntimeException("User not found: $username")
        
        return UserInfoDto(
            id = user.id,
            username = user.username,
            roles = user.roles
        )
    }

    fun getUserBids(status: BidStatus?, page: Int, size: Int): UserBidPage {
        TODO("Implement user bids retrieval")
    }

    fun getUserWatchlist(): List<LotSummary> {
        TODO("Implement user watchlist retrieval")
    }

    fun addToWatchlist(lotId: String) {
        TODO("Implement adding lot to watchlist")
    }

    fun removeFromWatchlist(lotId: String) {
        TODO("Implement removing lot from watchlist")
    }
}
