package com.procar.auction.util

import com.procar.provider.bid.BidStatus
import com.procar.provider.bid.BidType
import com.procar.provider.bid.PlaceBidRequest
import com.procar.provider.bid.ProviderBid
import java.time.LocalDateTime
import java.util.*

object BidsFactory {
    
    fun createErrorBid(request: PlaceBidRequest): ProviderBid {
        return ProviderBid(
            id = UUID.randomUUID().toString(),
            lotId = request.lotId,
            providerId = "procar",
            externalId = null,
            bidderId = request.bidderId,
            amount = request.amount,
            bidType = request.bidType,
            status = BidStatus.REJECTED,
            isWinning = false,
            isAutoBid = request.bidType == BidType.AUTO,
            placedAt = LocalDateTime.now()
        )
    }
}
