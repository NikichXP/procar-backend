package com.procar.auction.api

import com.procar.auction.factory.BidResponseFactory
import com.procar.auction.service.InternalAuctionBidService
import com.procar.provider.InternalBidAPI
import com.procar.provider.bid.BidAnalyticsResponse
import com.procar.provider.bid.BidHistoryResponse
import com.procar.provider.bid.PlaceBidRequest
import com.procar.provider.bid.PlaceBidResponse
import com.procar.provider.bid.ValidateBidRequest
import com.procar.provider.bid.ValidateBidResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class BidAPI(
    private val bidService: InternalAuctionBidService,
    private val responseFactory: BidResponseFactory
) : InternalBidAPI {

    override fun getBidHistory(lotId: String, connectionId: String?): ResponseEntity<BidHistoryResponse> {
        return try {
            val response = bidService.getBidHistory(lotId, connectionId)
            ResponseEntity.ok(response)
        } catch (_: Exception) {
            ResponseEntity.ok(responseFactory.createEmptyBidHistoryResponse(lotId))
        }
    }

    override fun placeBid(request: PlaceBidRequest): ResponseEntity<PlaceBidResponse> {
        return try {
            val response = bidService.placeBid(request)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.ok(responseFactory.createErrorPlaceBidResponse(request, e.message ?: "Unknown error"))
        }
    }

    override fun validateBid(request: ValidateBidRequest): ResponseEntity<ValidateBidResponse> {
        return try {
            val response = bidService.validateBid(request)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.ok(responseFactory.createErrorValidateBidResponse(e.message ?: "Unknown error"))
        }
    }

    override fun getBidAnalytics(lotId: String, timeRange: String?): ResponseEntity<BidAnalyticsResponse> {
        return try {
            val response = bidService.getBidAnalytics(lotId, timeRange)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.ok(responseFactory.createErrorBidAnalyticsResponse(lotId, e.message ?: "Unknown error"))
        }
    }
}
