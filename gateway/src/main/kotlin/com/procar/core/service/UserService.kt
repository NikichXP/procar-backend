package com.procar.core.service

import com.procar.core.converter.UserDtoToUserInfoDtoConverter
import com.procar.gateway.api.dto.BidStatus
import com.procar.gateway.api.dto.LotSummary
import com.procar.gateway.api.dto.UserBidPage
import com.procar.gateway.api.dto.UserInfoDto
import com.procar.core.service.admin.AdminUserConnectorService
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val adminUserConnectorService: AdminUserConnectorService,
    private val userDtoToUserInfoDtoConverter: UserDtoToUserInfoDtoConverter
) {

    suspend fun getCurrentUserId(): String {
        val authentication = ReactiveSecurityContextHolder.getContext()
            .map { it.authentication }
            .awaitFirst()

        return authentication?.name
            ?: throw RuntimeException("User not authenticated")
    }

    suspend fun getUserInfo(userId: String): UserInfoDto {
        val user = adminUserConnectorService.getUser(userId)
        return userDtoToUserInfoDtoConverter.convert(user)
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
