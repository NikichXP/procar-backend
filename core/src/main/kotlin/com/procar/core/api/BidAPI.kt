package com.procar.core.api

import com.procar.core.api.dto.Bid
import com.procar.core.api.dto.BidPage
import com.procar.core.api.dto.BidRequest
import com.procar.core.service.BidService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lots/{lotId}/bids")
class BidAPI(
    private val bidService: BidService
) {

    @GetMapping
    fun getBidHistory(
        @PathVariable lotId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): BidPage {
        return bidService.getBidHistory(lotId, page, size)
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    fun placeBid(
        @PathVariable lotId: String,
        @RequestBody bidRequest: BidRequest
    ): Bid {
        return bidService.placeBid(lotId, bidRequest)
    }
}
