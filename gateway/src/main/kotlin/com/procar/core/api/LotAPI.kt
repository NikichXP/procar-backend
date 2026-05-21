package com.procar.core.api

import com.procar.core.config.NoAuth
import com.procar.core.service.LotService
import com.procar.gateway.api.dto.*
import com.procar.provider.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@NoAuth
@Tag(name = "lot-api", description = "Browse and search car auction lots")
@RestController
@RequestMapping("/lots")
class LotAPI(
    private val lotService: LotService
) {

    @Operation(summary = "List lots", description = "Search and filter car auction lots with pagination.")
    @GetMapping
    suspend fun getLots(
        @Parameter(
            description = "Filter by lot status (comma-separated for multiple)",
            example = "ACTIVE,PENDING"
        ) @RequestParam(required = false) status: List<LotStatus>?,
        @Parameter(
            description = "Filter by auction source name",
            example = "Copart"
        ) @RequestParam(required = false) source: String?,
        @Parameter(
            description = "Filter lots ending within N minutes",
            example = "60"
        ) @RequestParam(required = false) endsInMinutes: Int?,
        @Parameter(description = "Filter by brand ID", example = "01956b0a-aaaa-7000-8000-000000000001") @RequestParam(
            required = false
        ) brandId: String?,
        @Parameter(description = "Filter by model ID", example = "01956b0a-bbbb-7000-8000-000000000002") @RequestParam(
            required = false
        ) modelId: String?,
        @Parameter(
            description = "Minimum manufacture year",
            example = "2018"
        ) @RequestParam(required = false) yearFrom: Int?,
        @Parameter(
            description = "Maximum manufacture year",
            example = "2023"
        ) @RequestParam(required = false) yearTo: Int?,
        @Parameter(
            description = "Minimum current bid (USD)",
            example = "5000.0"
        ) @RequestParam(required = false) priceFrom: Double?,
        @Parameter(
            description = "Maximum current bid (USD)",
            example = "30000.0"
        ) @RequestParam(required = false) priceTo: Double?,
        @Parameter(
            description = "Vehicle condition filter",
            example = "USED"
        ) @RequestParam(required = false) condition: CarCondition?,
        @Parameter(description = "Pagination cursor", example = "eyJvZmZzZXQiOjV9") @RequestParam(required = false) cursor: String?,
        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") limit: Int,
        @Parameter(
            description = "Sort field and direction",
            example = "endTime,asc"
        ) @RequestParam(defaultValue = "endTime,asc") sort: String
    ): LotPage {

        val actualStatuses = status?.filter { it in allowedStatuses }?.ifEmpty { allowedStatuses } ?: allowedStatuses

        val request = LotSearchRequest(
            statuses = actualStatuses,
            source = source,
            endsInMinutes = endsInMinutes,
            brandId = brandId,
            modelId = modelId,
            yearFrom = yearFrom,
            yearTo = yearTo,
            priceFrom = priceFrom,
            priceTo = priceTo,
            condition = condition,
            cursor = cursor,
            limit = limit,
            sort = sort
        )

        return lotService.getLots(request)
    }

    @Operation(
        summary = "Get lot detail",
        description = "Retrieve full details for a specific auction lot, including recent bids."
    )
    @GetMapping("/{lotId}")
    suspend fun getLotDetail(
        @Parameter(description = "Lot ID", example = "01956b0a-1234-7abc-9d2e-4f5a6b7c8d9e") @PathVariable lotId: String
    ): ApiResponse<LotDetail> {
        return ApiResponse(lotService.getLotDetail(lotId))
    }

    companion object {
        private val allowedStatuses = listOf(LotStatus.PENDING, LotStatus.ACTIVE)
    }
}
