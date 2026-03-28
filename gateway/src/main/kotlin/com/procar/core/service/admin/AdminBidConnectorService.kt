package com.procar.core.service.admin

import com.procar.provider.admin.AdminBidController
import com.procar.provider.bid.BidAnalyticsResponse
import com.procar.provider.bid.BidHistoryResponse
import com.procar.provider.common.ApiResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class AdminBidConnectorService(
    @Qualifier("adminBidHttpClient") private val adminBidController: AdminBidController
) {

    suspend fun getBidHistoryForLot(lotId: String): ResponseEntity<ApiResponse<BidHistoryResponse>> {
        return adminBidController.getBidHistoryForLot(lotId)
    }

    suspend fun getBidAnalyticsForLot(lotId: String, timeRange: String?): ResponseEntity<ApiResponse<BidAnalyticsResponse>> {
        return adminBidController.getBidAnalyticsForLot(lotId, timeRange)
    }

    suspend fun deleteAllBidsForLot(lotId: String): ResponseEntity<ApiResponse<Void?>> {
        return adminBidController.deleteAllBidsForLot(lotId)
    }
}
