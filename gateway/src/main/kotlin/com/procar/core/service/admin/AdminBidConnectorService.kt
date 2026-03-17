package com.procar.core.service.admin

import com.procar.provider.admin.AdminBidController
import com.procar.provider.bid.BidAnalyticsResponse
import com.procar.provider.bid.BidHistoryResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class AdminBidConnectorService(
    @Qualifier("adminBidHttpClient") private val adminBidController: AdminBidController
) {

    fun getBidHistoryForLot(lotId: String): ResponseEntity<BidHistoryResponse> {
        return adminBidController.getBidHistoryForLot(lotId)
    }

    fun getBidAnalyticsForLot(lotId: String, timeRange: String?): ResponseEntity<BidAnalyticsResponse> {
        return adminBidController.getBidAnalyticsForLot(lotId, timeRange)
    }

    fun deleteAllBidsForLot(lotId: String): ResponseEntity<Void> {
        return adminBidController.deleteAllBidsForLot(lotId)
    }
}
