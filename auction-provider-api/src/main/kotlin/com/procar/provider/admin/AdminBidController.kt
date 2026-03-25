package com.procar.provider.admin

import com.procar.provider.common.*
import com.procar.provider.bid.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@RequestMapping("/api/admin/bids")
interface AdminBidController {

    @GetMapping("/lot/{lotId}")
    fun getBidHistoryForLot(@PathVariable lotId: String): ResponseEntity<ApiResponse<BidHistoryResponse>>

    @GetMapping("/lot/{lotId}/analytics")
    fun getBidAnalyticsForLot(
        @PathVariable lotId: String,
        @RequestParam timeRange: String?
    ): ResponseEntity<ApiResponse<BidAnalyticsResponse>>

    @DeleteMapping("/lot/{lotId}")
    fun deleteAllBidsForLot(@PathVariable lotId: String): ResponseEntity<ApiResponse<Void?>>
}
