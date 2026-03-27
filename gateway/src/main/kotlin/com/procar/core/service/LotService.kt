package com.procar.core.service

import com.procar.core.api.dto.*
import com.procar.provider.InternalLotAPI
import com.procar.provider.common.*
import com.procar.provider.lot.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter
import com.procar.provider.lot.LotStatus as ProviderLotStatus

@Service
class LotService(
    @Qualifier("internalLotHttpClient") private val internalLotAPI: InternalLotAPI
) {

    fun getLots(request: LotSearchRequest): List<LotSummary> {
        // Convert gateway search request to internal advanced search request
        val advancedRequest = AdvancedLotSearchRequest(
            query = null, // TODO: Map query if needed
            filters = LotSearchFilters(
                status = request.status?.let { listOf(mapStatus(it)) },
                priceRange = request.priceFrom?.let { from ->
                    request.priceTo?.let { to ->
                        PriceRange(from, to)
                    }
                },
                yearRange = request.yearFrom?.let { from ->
                    request.yearTo?.let { to ->
                        from..to
                    }
                },
                vehicle = VehicleFilters(
                    makes = request.brandId?.let { listOf(it) },
                    models = request.modelId?.let { listOf(it) }
                )
            ),
            pagination = PaginationRequest(
                limit = request.limit,
                cursor = null
            )
        )
        
        val response: ResponseEntity<ApiResponse<LotSearchResponse>> = internalLotAPI.searchLots(advancedRequest)
        val apiResponse = response.body ?: throw RuntimeException("Failed to search lots")
        val searchResult = apiResponse.data
        
        // Convert internal response to gateway DTO
        return searchResult.results.map { vehicleLot ->
            LotSummary(
                id = vehicleLot.id,
                car = CarInfo(
                    brandId = vehicleLot.vehicle.make,
                    brandName = vehicleLot.vehicle.make,
                    modelId = vehicleLot.vehicle.model,
                    modelName = vehicleLot.vehicle.model,
                    year = vehicleLot.vehicle.year,
                    condition = mapCondition(vehicleLot.vehicle.condition),
                    mileage = vehicleLot.vehicle.mileage,
                    vin = vehicleLot.vehicle.vin,
                    color = vehicleLot.vehicle.color,
                    description = vehicleLot.description,
                    images = vehicleLot.vehicle.images.filter { it.isPrimary }.map { it.url }
                ),
                status = mapStatusToGateway(vehicleLot.status),
                currentBid = vehicleLot.auction.currentBid,
                startingBid = vehicleLot.auction.startingBid,
                bidStep = vehicleLot.auction.bidIncrement,
                bidsCount = vehicleLot.auction.totalBids,
                startTime = vehicleLot.auction.startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                endTime = vehicleLot.auction.endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                source = LotSource(
                    name = vehicleLot.providerName,
                    lotUrl = "https://example.com/lot/${vehicleLot.id}" // TODO: Generate proper URL
                )
            )
        }
    }

    fun countLots(request: LotSearchRequest): Int {
        // For now, return the size of the search results
        // TODO: Implement dedicated count endpoint when available in internal API
        return getLots(request).size
    }

    fun getLotDetail(lotId: String): LotDetail {
        val response: ResponseEntity<ApiResponse<VehicleLot>> = internalLotAPI.getLotDetail(lotId)
        val apiResponse = response.body ?: throw RuntimeException("Failed to get lot detail")
        val vehicleLot = apiResponse.data
        
        // Convert internal response to gateway DTO
        return LotDetail(
            id = vehicleLot.id,
            car = CarInfo(
                brandId = vehicleLot.vehicle.make,
                brandName = vehicleLot.vehicle.make,
                modelId = vehicleLot.vehicle.model,
                modelName = vehicleLot.vehicle.model,
                year = vehicleLot.vehicle.year,
                condition = mapCondition(vehicleLot.vehicle.condition),
                mileage = vehicleLot.vehicle.mileage,
                vin = vehicleLot.vehicle.vin,
                color = vehicleLot.vehicle.color,
                description = vehicleLot.description,
                images = vehicleLot.vehicle.images.map { it.url },
                documents = CarDocuments(
                    hasTitle = vehicleLot.vehicle.documents.any { it.type == DocumentType.TITLE },
                    titleState = null // TODO: Extract from document data
                )
            ),
            status = mapStatusToGateway(vehicleLot.status),
            currentBid = vehicleLot.auction.currentBid,
            startingBid = vehicleLot.auction.startingBid,
            bidStep = vehicleLot.auction.bidIncrement,
            bidsCount = vehicleLot.auction.totalBids,
            startTime = vehicleLot.auction.startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            endTime = vehicleLot.auction.endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            source = LotSource(
                name = vehicleLot.providerName,
                lotUrl = "https://example.com/lot/${vehicleLot.id}" // TODO: Generate proper URL
            ),
            location = LotLocation(
                city = vehicleLot.location.city,
                state = vehicleLot.location.state,
                country = vehicleLot.location.country
            ),
            fees = LotFees(
                buyerPremiumPercent = vehicleLot.metadata.fees
                    .find { it.type == FeeType.BUYER_PREMIUM }
                    ?.amount,
                documentationFee = vehicleLot.metadata.fees
                    .find { it.type == FeeType.DOCUMENTATION }
                    ?.amount
            ),
            recentBids = emptyList() // TODO: Fetch recent bids from bid API
        )
    }

    private fun mapStatus(status: LotStatus): ProviderLotStatus {
        return when (status) {
            LotStatus.UPCOMING -> ProviderLotStatus.UPCOMING
            LotStatus.ACTIVE -> ProviderLotStatus.ACTIVE
            LotStatus.FINISHED -> ProviderLotStatus.ENDED
            LotStatus.CANCELLED -> ProviderLotStatus.CANCELLED
        }
    }

    private fun mapStatusToGateway(status: ProviderLotStatus): LotStatus {
        return when (status) {
            ProviderLotStatus.UPCOMING -> LotStatus.UPCOMING
            ProviderLotStatus.ACTIVE -> LotStatus.ACTIVE
            ProviderLotStatus.ENDED,
            ProviderLotStatus.SOLD,
            ProviderLotStatus.UNSOLD -> LotStatus.FINISHED
            ProviderLotStatus.CANCELLED,
            ProviderLotStatus.SUSPENDED -> LotStatus.CANCELLED
            else -> LotStatus.CANCELLED // Default fallback
        }
    }

    private fun mapCondition(condition: VehicleCondition): CarCondition {
        return when (condition) {
            VehicleCondition.EXCELLENT,
            VehicleCondition.GOOD -> CarCondition.USED
            VehicleCondition.FAIR,
            VehicleCondition.POOR -> CarCondition.USED
            VehicleCondition.DAMAGED,
            VehicleCondition.SALVAGE -> CarCondition.DAMAGED
        }
    }
}
