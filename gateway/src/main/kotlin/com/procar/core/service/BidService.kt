package com.procar.core.service

import com.procar.gateway.api.dto.Bid
import com.procar.gateway.api.dto.BidPage
import com.procar.gateway.api.dto.BidRequest
import com.procar.gateway.api.dto.BidStatus.*
import com.procar.gateway.api.dto.PlaceBidResult
import com.procar.provider.InternalBidAPI
import com.procar.provider.bid.*
import com.procar.provider.common.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class BidService(
    @Qualifier("internalBidHttpClient") private val internalBidAPI: InternalBidAPI
) {

    suspend fun getBidHistory(lotId: String, page: Int, size: Int): BidPage = withContext(Dispatchers.IO) {
        val response: ResponseEntity<ApiResponse<BidHistoryResponse>> = internalBidAPI.getBidHistory(lotId)
        val apiResponse = response.body ?: throw RuntimeException("Failed to get bid history")
        val bidHistory = apiResponse.data
        
        // Convert internal response to gateway DTO
        BidPage(
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
                    status = valueOf(internalBid.status.name)
                )
            }
        )
    }

    suspend fun placeBid(lotId: String, bidRequest: BidRequest, bidderId: String): PlaceBidResult = withContext(Dispatchers.IO) {
        val placeRequest = PlaceBidRequest(
            lotId = lotId,
            amount = bidRequest.amount,
            bidderId = bidderId
        )
        val placeResponse: ResponseEntity<ApiResponse<PlaceBidResponse>> = internalBidAPI.placeBid(placeRequest)
        val placedBid = placeResponse.body?.data
            ?: return@withContext PlaceBidResult(status = REJECTED, message = "Failed to place bid")

        if (placedBid.status == BidStatus.REJECTED) {
            return@withContext PlaceBidResult(status = REJECTED, message = placedBid.message)
        }

        val bid = Bid(
            id = placedBid.bid.id,
            lotId = placedBid.bid.lotId,
            amount = placedBid.bid.amount,
            bidderId = placedBid.bid.bidderId,
            placedAt = placedBid.bid.placedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            isWinning = placedBid.isWinning,
            status = valueOf(placedBid.bid.status.name)
        )
        PlaceBidResult(bid = bid, status = bid.status, message = placedBid.message)
    }
}
