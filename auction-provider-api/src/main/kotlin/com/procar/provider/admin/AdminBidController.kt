package com.procar.provider.admin

import com.procar.provider.bid.BidAnalyticsResponse
import com.procar.provider.bid.BidHistoryResponse
import com.procar.provider.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.DeleteExchange
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange("/api/admin/bids")
interface AdminBidController {

    @GetExchange("/lot/{lotId}")
    fun getBidHistoryForLot(@PathVariable lotId: String): ResponseEntity<ApiResponse<BidHistoryResponse>>

    @GetExchange("/lot/{lotId}/analytics")
    fun getBidAnalyticsForLot(
        @PathVariable lotId: String,
        @RequestParam timeRange: String?
    ): ResponseEntity<ApiResponse<BidAnalyticsResponse>>

    @DeleteExchange("/lot/{lotId}")
    fun deleteAllBidsForLot(@PathVariable lotId: String): ResponseEntity<ApiResponse<Void?>>
}
