package com.procar.core.service

import com.procar.core.converter.UserDtoToUserInfoDtoConverter
import com.procar.core.service.admin.AdminUserConnectorService
import com.procar.gateway.api.dto.*
import com.procar.provider.InternalBidAPI
import com.procar.provider.bid.BidStatus as ProviderBidStatus
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class UserService(
    private val adminUserConnectorService: AdminUserConnectorService,
    private val userDtoToUserInfoDtoConverter: UserDtoToUserInfoDtoConverter,
    @Qualifier("internalBidHttpClient") private val internalBidAPI: InternalBidAPI,
    private val lotService: LotService
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

    suspend fun getUserBids(status: BidStatus?, page: Int, size: Int): UserBidPage {
        val userId = getCurrentUserId()
        val providerStatus = status?.let { ProviderBidStatus.valueOf(it.name) }
        val response = internalBidAPI.getUserBids(userId, providerStatus, page, size)
        val apiResponse = response.body ?: throw RuntimeException("Failed to get user bids")
        val data = apiResponse.data

        val userBids = data.bids.map { bid ->
            val lotSummary = lotService.getLotSummary(bid.lotId)
            UserBid(
                id = bid.id,
                lotId = bid.lotId,
                amount = bid.amount,
                bidderId = bid.bidderId,
                placedAt = bid.placedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                isWinning = bid.isWinning,
                lot = lotSummary,
                bidStatus = BidStatus.valueOf(bid.status.name)
            )
        }

        return UserBidPage(
            page = page,
            size = size,
            totalElements = data.summary.bidCount,
            totalPages = (data.summary.bidCount + size - 1) / size,
            content = userBids
        )
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
