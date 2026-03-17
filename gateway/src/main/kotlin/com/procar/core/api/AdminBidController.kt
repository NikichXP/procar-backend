package com.procar.core.api

import com.procar.core.service.admin.AdminBidConnectorService
import com.procar.provider.admin.AdminBidController
import com.procar.provider.bid.BidAnalyticsResponse
import com.procar.provider.bid.BidHistoryResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/bids")
class AdminBidController(
    private val adminBidConnectorService: AdminBidConnectorService
) : AdminBidController {

    @GetMapping("/lot/{lotId}")
    override fun getBidHistoryForLot(@PathVariable lotId: String): ResponseEntity<BidHistoryResponse> {
        return adminBidConnectorService.getBidHistoryForLot(lotId)
    }

    @GetMapping("/lot/{lotId}/analytics")
    override fun getBidAnalyticsForLot(
        @PathVariable lotId: String,
        @RequestParam timeRange: String?
    ): ResponseEntity<BidAnalyticsResponse> {
        return adminBidConnectorService.getBidAnalyticsForLot(lotId, timeRange)
    }

    @DeleteMapping("/lot/{lotId}")
    override fun deleteAllBidsForLot(@PathVariable lotId: String): ResponseEntity<Void> {
        return adminBidConnectorService.deleteAllBidsForLot(lotId)
    }
}
