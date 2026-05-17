package com.procar.core.service

import com.procar.gateway.api.dto.Bid
import com.procar.gateway.api.dto.BidPage
import com.procar.gateway.api.dto.BidRequest
import com.procar.gateway.api.dto.BuyoutResult
import com.procar.provider.InternalBidAPI
import com.procar.provider.bid.*
import com.procar.provider.common.ApiResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class BidService(
    @Qualifier("internalBidHttpClient") private val internalBidAPI: InternalBidAPI
) {

    fun getBidHistory(lotId: String, page: Int, size: Int): BidPage {
        val response: ResponseEntity<ApiResponse<BidHistoryResponse>> = internalBidAPI.getBidHistory(lotId)
        val apiResponse = response.body ?: throw RuntimeException("Failed to get bid history")
        val bidHistory = apiResponse.data
        
        // Convert internal response to gateway DTO
        return BidPage(
            page = page,
            size = size,
            totalElements = bidHistory.bids.size,
            totalPages = 1, // TODO: Calculate from response when pagination is supported
            content = bidHistory.bids.map { internalBid ->
                Bid(
                    id = internalBid.id,
                    lotId = internalBid.lotId,
                    amount = internalBid.amount,
                    bidderId = internalBid.bidderId,
                    placedAt = internalBid.placedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    isWinning = internalBid.isWinning,
                    status = com.procar.gateway.api.dto.BidStatus.valueOf(internalBid.status.name)
                )
            }
        )
    }

    fun placeBid(lotId: String, bidRequest: BidRequest, bidderId: String): Bid {
        // Validate bid first
        val validateRequest = ValidateBidRequest(
            lotId = lotId,
            amount = bidRequest.amount,
            bidderId = bidderId
        )
        val validateResponse: ResponseEntity<ApiResponse<ValidateBidResponse>> = internalBidAPI.validateBid(validateRequest)
        val validateApiResult = validateResponse.body ?: throw RuntimeException("Failed to validate bid")
        val validationResult = validateApiResult.data
        
        if (!validationResult.isValid) {
            throw RuntimeException("Bid validation failed: ${validationResult.message}")
        }
        
        // Place the bid
        val placeRequest = PlaceBidRequest(
            lotId = lotId,
            amount = bidRequest.amount,
            bidderId = bidderId
        )
        val placeRequestWithSource = placeRequest // Ensure it's correctly built if needed
        val placeResponse: ResponseEntity<ApiResponse<PlaceBidResponse>> = internalBidAPI.placeBid(placeRequest)
        val placeApiResult = placeResponse.body ?: throw RuntimeException("Failed to place bid")
        val placedBid = placeApiResult.data
        
        return Bid(
            id = placedBid.bid.id,
            lotId = placedBid.bid.lotId,
            amount = placedBid.bid.amount,
            bidderId = placedBid.bid.bidderId,
            placedAt = placedBid.bid.placedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            isWinning = placedBid.bid.isWinning,
            status = com.procar.gateway.api.dto.BidStatus.valueOf(placedBid.bid.status.name)
        )
    }

    fun buyout(lotId: String, bidderId: String): BuyoutResult {
        val body = internalBidAPI.buyout(BuyoutRequest(lotId, bidderId)).body
            ?: throw RuntimeException("Failed to buyout")
        val data = body.data
        // We allow both WON and ACCEPTED for backward compatibility, but we transition to WON
        if (data.status != BidStatus.WON && data.status != BidStatus.ACCEPTED) {
            throw org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT,
                data.message ?: "Buyout rejected",
            )
        }
        return BuyoutResult(
            lotId = data.lotId,
            price = data.price,
            purchasedAt = data.purchasedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            status = com.procar.gateway.api.dto.BidStatus.valueOf(data.status.name)
        )
    }
}
