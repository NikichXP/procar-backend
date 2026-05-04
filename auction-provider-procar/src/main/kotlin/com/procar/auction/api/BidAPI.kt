package com.procar.auction.api

import com.procar.auction.factory.BidResponseFactory
import com.procar.auction.service.BuyoutService
import com.procar.auction.service.InternalAuctionBidService
import com.procar.provider.InternalBidAPI
import com.procar.provider.bid.*
import com.procar.provider.common.ApiResponse
import com.procar.provider.common.PaginationResponse
import org.springframework.core.convert.ConversionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class BidAPI(
    private val bidService: InternalAuctionBidService,
    private val buyoutService: BuyoutService,
    private val responseFactory: BidResponseFactory,
    private val conversionService: ConversionService
) : InternalBidAPI {

    override fun getBidHistory(lotId: String): ResponseEntity<ApiResponse<BidHistoryResponse>> {
        return try {
            val bids = bidService.getAllBidsForLot(lotId)
                .mapNotNull { conversionService.convert(it, ProviderBid::class.java) }
            val active = bids.filter { it.status == BidStatus.ACCEPTED || it.status == BidStatus.WON }
            val summary = BidSummary(
                totalBids = bids.size,
                currentBid = active.maxOfOrNull { it.amount } ?: 0.0,
                bidCount = active.size,
                highestBid = active.maxOfOrNull { it.amount } ?: 0.0,
                lowestBid = active.minOfOrNull { it.amount } ?: 0.0,
                averageBid = if (active.isNotEmpty()) active.map { it.amount }.average() else 0.0
            )
            ResponseEntity.ok(ApiResponse(BidHistoryResponse(
                lotId = lotId,
                bids = bids,
                pagination = PaginationResponse(hasNext = false, nextCursor = null),
                summary = summary
            )))
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

    override fun buyout(request: BuyoutRequest): ResponseEntity<ApiResponse<BuyoutResponse>> {
        return try {
            ResponseEntity.ok(ApiResponse(buyoutService.buyout(request)))
        } catch (e: Exception) {
            ResponseEntity.ok(
                ApiResponse(
                    BuyoutResponse(
                        lotId = request.lotId,
                        bidderId = request.bidderId,
                        price = 0.0,
                        purchasedAt = java.time.LocalDateTime.now(),
                        status = BidStatus.REJECTED,
                        message = e.message ?: "Unknown error",
                    ),
                    e.message,
                ),
            )
        }
    }
}
