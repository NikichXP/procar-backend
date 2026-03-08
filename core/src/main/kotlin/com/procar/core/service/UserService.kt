package com.procar.core.service

import com.procar.core.api.dto.BidStatus
import com.procar.core.api.dto.LotSummary
import com.procar.core.api.dto.UserBidPage
import org.springframework.stereotype.Service

@Service
class UserService {

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
