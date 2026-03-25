package com.procar.auction.api

import com.procar.auction.factory.BidResponseFactory
import com.procar.auction.service.InternalAuctionBidService
import com.procar.provider.InternalBidAPI
import com.procar.provider.bid.*
import com.procar.provider.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class BidAPI(
    private val bidService: InternalAuctionBidService,
    private val responseFactory: BidResponseFactory
) : InternalBidAPI {

    override fun getBidHistory(lotId: String, connectionId: String?): ResponseEntity<ApiResponse<BidHistoryResponse>> {
        return try {
            val response = bidService.getBidHistory(lotId, connectionId)
            ResponseEntity.ok(ApiResponse(response))
        } catch (_: Exception) {
            ResponseEntity.ok(ApiResponse(responseFactory.createEmptyBidHistoryResponse(lotId)))
        }
    }

    override fun placeBid(request: PlaceBidRequest): ResponseEntity<ApiResponse<PlaceBidResponse>> {
        return try {
            val response = bidService.placeBid(request)
            ResponseEntity.ok(ApiResponse(response))
        } catch (e: Exception) {
            ResponseEntity.ok(ApiResponse(responseFactory.createErrorPlaceBidResponse(request, e.message ?: "Unknown error"), e.message))
        }
    }

    override fun validateBid(request: ValidateBidRequest): ResponseEntity<ApiResponse<ValidateBidResponse>> {
        return try {
            val response = bidService.validateBid(request)
            ResponseEntity.ok(ApiResponse(response))
        } catch (e: Exception) {
            ResponseEntity.ok(ApiResponse(responseFactory.createErrorValidateBidResponse(e.message ?: "Unknown error"), e.message))
        }
    }

    override fun getBidAnalytics(lotId: String, timeRange: String?): ResponseEntity<ApiResponse<BidAnalyticsResponse>> {
        return try {
            val response = bidService.getBidAnalytics(lotId, timeRange)
            ResponseEntity.ok(ApiResponse(response))
        } catch (e: Exception) {
            ResponseEntity.ok(ApiResponse(responseFactory.createErrorBidAnalyticsResponse(lotId, e.message ?: "Unknown error"), e.message))
        }
    }
}
