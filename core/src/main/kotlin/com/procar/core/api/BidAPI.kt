package com.procar.core.api

import com.procar.core.api.dto.Bid
import com.procar.core.api.dto.BidPage
import com.procar.core.api.dto.BidRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lots/{lotId}/bids")
class BidAPI {

    @GetMapping
    fun getBidHistory(
        @PathVariable lotId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): BidPage {
        TODO("Implement bid history retrieval")
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    fun placeBid(
        @PathVariable lotId: String,
        @RequestBody bidRequest: BidRequest
    ): Bid {
        TODO("Implement bid placement")
    }
}
