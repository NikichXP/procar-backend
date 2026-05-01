package com.procar.core.api

import com.procar.gateway.api.dto.BidStatus
import com.procar.gateway.api.dto.LotSummary
import com.procar.gateway.api.dto.UserBidPage
import com.procar.core.service.UserService
import com.procar.user.api.dto.UserInfoDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "user-api", description = "Current user profile, bids and watchlist")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/users/me")
class UserAPI(
    private val userService: UserService
) {

    @Operation(summary = "Get current user", description = "Returns the authenticated user's profile information.")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    suspend fun getCurrentUser(): UserInfoDto {
        val userId = userService.getCurrentUserId()
        return userService.getUserInfo(userId)
    }

    @Operation(summary = "Get user bids", description = "Returns a paginated list of bids placed by the current user.")
    @GetMapping("/bids")
    @PreAuthorize("isAuthenticated()")
    fun getUserBids(
        @Parameter(
            description = "Filter by bid status",
            example = "WINNING"
        ) @RequestParam(required = false) status: BidStatus?,
        @Parameter(description = "Zero-based page number", example = "0") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") size: Int
    ): UserBidPage {
        return userService.getUserBids(status, page, size)
    }

    @Operation(summary = "Get watchlist", description = "Returns the list of lots the current user is watching.")
    @GetMapping("/watchlist")
    @PreAuthorize("isAuthenticated()")
    fun getUserWatchlist(): List<LotSummary> {
        return userService.getUserWatchlist()
    }

    @Operation(summary = "Add to watchlist", description = "Add a lot to the current user's watchlist.")
    @PutMapping("/watchlist/{lotId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addToWatchlist(
        @Parameter(description = "Lot ID", example = "01956b0a-1234-7abc-9d2e-4f5a6b7c8d9e") @PathVariable lotId: String
    ) {
        userService.addToWatchlist(lotId)
    }

    @Operation(summary = "Remove from watchlist", description = "Remove a lot from the current user's watchlist.")
    @DeleteMapping("/watchlist/{lotId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeFromWatchlist(
        @Parameter(description = "Lot ID", example = "01956b0a-1234-7abc-9d2e-4f5a6b7c8d9e") @PathVariable lotId: String
    ) {
        userService.removeFromWatchlist(lotId)
    }
}
