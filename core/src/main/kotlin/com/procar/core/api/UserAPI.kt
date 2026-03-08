package com.procar.core.api

import com.procar.core.api.dto.BidStatus
import com.procar.core.api.dto.LotSummary
import com.procar.core.api.dto.UserBidPage
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users/me")
class UserAPI {

    @GetMapping("/bids")
    @PreAuthorize("isAuthenticated()")
    fun getUserBids(
        @RequestParam(required = false) status: BidStatus?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): UserBidPage {
        TODO("Implement user bids retrieval")
    }

    @GetMapping("/watchlist")
    @PreAuthorize("isAuthenticated()")
    fun getUserWatchlist(): List<LotSummary> {
        TODO("Implement user watchlist retrieval")
    }

    @PutMapping("/watchlist/{lotId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addToWatchlist(@PathVariable lotId: String) {
        TODO("Implement adding lot to watchlist")
    }

    @DeleteMapping("/watchlist/{lotId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeFromWatchlist(@PathVariable lotId: String) {
        TODO("Implement removing lot from watchlist")
    }
}
