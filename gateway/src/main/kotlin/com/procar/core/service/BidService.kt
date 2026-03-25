package com.procar.core.service

import com.procar.core.api.dto.*
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
        val response: ResponseEntity<ApiResponse<BidHistoryResponse>> = internalBidAPI.getBidHistory(lotId, null)
        val apiResponse = response.body ?: throw RuntimeException("Failed to get bid history")
        val bidHistory = apiResponse.data ?: throw RuntimeException("Failed to get bid history data")
        
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
                    isWinning = internalBid.isWinning
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
        val validationResult = validateApiResult.data ?: throw RuntimeException("Failed to get validation data")
        
        if (!validationResult.isValid) {
            throw RuntimeException("Bid validation failed: ${validationResult.message}")
        }
        
        // Place the bid
        val placeRequest = PlaceBidRequest(
            lotId = lotId,
            amount = bidRequest.amount,
            bidderId = bidderId
        )
        val placeResponse: ResponseEntity<ApiResponse<PlaceBidResponse>> = internalBidAPI.placeBid(placeRequest)
        val placeApiResult = placeResponse.body ?: throw RuntimeException("Failed to place bid")
        val placedBid = placeApiResult.data ?: throw RuntimeException("Failed to get placed bid data")
        
        return Bid(
            id = placedBid.bid.id,
            lotId = placedBid.bid.lotId,
            amount = placedBid.bid.amount,
            bidderId = placedBid.bid.bidderId,
            placedAt = placedBid.bid.placedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            isWinning = placedBid.bid.isWinning
        )
    }

    fun validateBid(lotId: String, amount: Double, bidderId: String): Boolean {
        val request = ValidateBidRequest(
            lotId = lotId,
            amount = amount,
            bidderId = bidderId
        )
        val response: ResponseEntity<ApiResponse<ValidateBidResponse>> = internalBidAPI.validateBid(request)
        val apiResult = response.body
        return apiResult?.data?.isValid ?: false
    }
}
