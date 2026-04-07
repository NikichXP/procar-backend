package com.procar.core.api

import com.procar.core.api.dto.BidStatus
import com.procar.core.api.dto.LotSummary
import com.procar.core.api.dto.UserBidPage
import com.procar.core.service.UserService
import com.procar.user.api.dto.UserInfoDto
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users/me")
class UserAPI(
    private val userService: UserService
) {

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    suspend fun getCurrentUser(): UserInfoDto {
        val userId = userService.getCurrentUserId()
        return userService.getUserInfo(userId)
    }

    @GetMapping("/bids")
    @PreAuthorize("isAuthenticated()")
    fun getUserBids(
        @RequestParam(required = false) status: BidStatus?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): UserBidPage {
        return userService.getUserBids(status, page, size)
    }

    @GetMapping("/watchlist")
    @PreAuthorize("isAuthenticated()")
    fun getUserWatchlist(): List<LotSummary> {
        return userService.getUserWatchlist()
    }

    @PutMapping("/watchlist/{lotId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addToWatchlist(@PathVariable lotId: String) {
        userService.addToWatchlist(lotId)
    }

    @DeleteMapping("/watchlist/{lotId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeFromWatchlist(@PathVariable lotId: String) {
        userService.removeFromWatchlist(lotId)
    }
}
