package com.procar.core.api

import com.procar.core.config.NoAuth
import com.procar.gateway.api.dto.BidPage
import com.procar.gateway.api.dto.BidRequest
import com.procar.gateway.api.dto.PlaceBidResult
import com.procar.core.service.BidService
import com.procar.provider.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*


@Tag(name = "bid-api", description = "Place bids and view bid history for auction lots")
@RestController
@RequestMapping("/api/lots/{lotId}")
class BidAPI(
    private val bidService: BidService
) {

    @NoAuth
    @Operation(summary = "Get bid history", description = "Retrieve paginated bid history for a specific lot.")
    @GetMapping("/bids")
    suspend fun getBidHistory(
        @Parameter(
            description = "Lot ID",
            example = "01956b0a-1234-7abc-9d2e-4f5a6b7c8d9e"
        ) @PathVariable lotId: String,
        @Parameter(description = "Zero-based page number", example = "0") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") size: Int
    ): BidPage {
        return bidService.getBidHistory(lotId, page, size)
    }

    @Operation(summary = "Place a bid", description = "Place a new bid on an active lot. Requires authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/bids")
    suspend fun placeBid(
        @Parameter(
            description = "Lot ID",
            example = "01956b0a-1234-7abc-9d2e-4f5a6b7c8d9e"
        ) @PathVariable lotId: String,
        @RequestBody bidRequest: BidRequest,
        authentication: Authentication
    ): ApiResponse<PlaceBidResult> {
        val result = bidService.placeBid(lotId, bidRequest, authentication.name)
        return ApiResponse(result)
    }
}
