package com.procar.core.api

import com.procar.core.service.admin.AdminBidConnectorService
import com.procar.provider.admin.AdminBidController
import com.procar.provider.bid.BidAnalyticsResponse
import com.procar.provider.bid.BidHistoryResponse
import com.procar.provider.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/bids")
class AdminBidController(
    private val adminBidConnectorService: AdminBidConnectorService
) : AdminBidController {

    @GetMapping("/lot/{lotId}")
    override fun getBidHistoryForLot(@PathVariable lotId: String): ResponseEntity<ApiResponse<BidHistoryResponse>> {
        return adminBidConnectorService.getBidHistoryForLot(lotId)
    }

    @GetMapping("/lot/{lotId}/analytics")
    override fun getBidAnalyticsForLot(
        @PathVariable lotId: String,
        @RequestParam timeRange: String?
    ): ResponseEntity<ApiResponse<BidAnalyticsResponse>> {
        return adminBidConnectorService.getBidAnalyticsForLot(lotId, timeRange)
    }

    @DeleteMapping("/lot/{lotId}")
    override fun deleteAllBidsForLot(@PathVariable lotId: String): ResponseEntity<ApiResponse<Void?>> {
        return adminBidConnectorService.deleteAllBidsForLot(lotId)
    }
}
