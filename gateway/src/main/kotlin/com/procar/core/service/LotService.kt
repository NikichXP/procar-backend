package com.procar.core.service

import com.procar.core.api.dto.CarCondition
import com.procar.core.api.dto.CarDocuments
import com.procar.core.api.dto.CarInfo
import com.procar.core.api.dto.LotDetail
import com.procar.core.api.dto.LotFees
import com.procar.core.api.dto.LotLocation
import com.procar.core.api.dto.LotSearchRequest
import com.procar.core.api.dto.LotSource
import com.procar.core.api.dto.LotStatus
import com.procar.core.api.dto.LotSummary
import com.procar.provider.InternalLotAPI
import com.procar.provider.common.PaginationRequest
import com.procar.provider.common.PriceRange
import com.procar.provider.lot.AdvancedLotSearchRequest
import com.procar.provider.lot.DocumentType
import com.procar.provider.lot.FeeType
import com.procar.provider.lot.LotSearchFilters
import com.procar.provider.lot.LotSearchResponse
import com.procar.provider.lot.ProviderLot
import com.procar.provider.lot.VehicleCondition
import com.procar.provider.lot.VehicleFilters
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
                size = request.limit,
                cursor = null
            )
        )
        
        val response: ResponseEntity<LotSearchResponse> = internalLotAPI.searchLots(advancedRequest)
        val searchResult = response.body ?: throw RuntimeException("Failed to search lots")
        
        // Convert internal response to gateway DTO
        return searchResult.results.map { providerLot ->
            LotSummary(
                id = providerLot.id,
                car = CarInfo(
                    brandId = providerLot.vehicle.make,
                    brandName = providerLot.vehicle.make,
                    modelId = providerLot.vehicle.model,
                    modelName = providerLot.vehicle.model,
                    year = providerLot.vehicle.year,
                    condition = mapCondition(providerLot.vehicle.condition),
                    mileage = providerLot.vehicle.mileage,
                    vin = providerLot.vehicle.vin,
                    color = providerLot.vehicle.color,
                    description = providerLot.description,
                    images = providerLot.vehicle.images.filter { it.isPrimary }.map { it.url }
                ),
                status = mapStatusToGateway(providerLot.status),
                currentBid = providerLot.auction.currentBid,
                startingBid = providerLot.auction.startingBid,
                bidStep = providerLot.auction.bidIncrement,
                bidsCount = providerLot.auction.totalBids,
                startTime = providerLot.auction.startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                endTime = providerLot.auction.endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                source = LotSource(
                    name = providerLot.providerName,
                    lotUrl = "https://example.com/lot/${providerLot.id}" // TODO: Generate proper URL
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
        val response: ResponseEntity<ProviderLot> = internalLotAPI.getLotDetail(lotId)
        val providerLot = response.body ?: throw RuntimeException("Failed to get lot detail")
        
        // Convert internal response to gateway DTO
        return LotDetail(
            id = providerLot.id,
            car = CarInfo(
                brandId = providerLot.vehicle.make,
                brandName = providerLot.vehicle.make,
                modelId = providerLot.vehicle.model,
                modelName = providerLot.vehicle.model,
                year = providerLot.vehicle.year,
                condition = mapCondition(providerLot.vehicle.condition),
                mileage = providerLot.vehicle.mileage,
                vin = providerLot.vehicle.vin,
                color = providerLot.vehicle.color,
                description = providerLot.description,
                images = providerLot.vehicle.images.map { it.url },
                documents = CarDocuments(
                    hasTitle = providerLot.vehicle.documents.any { it.type == DocumentType.TITLE },
                    titleState = null // TODO: Extract from document data
                )
            ),
            status = mapStatusToGateway(providerLot.status),
            currentBid = providerLot.auction.currentBid,
            startingBid = providerLot.auction.startingBid,
            bidStep = providerLot.auction.bidIncrement,
            bidsCount = providerLot.auction.totalBids,
            startTime = providerLot.auction.startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            endTime = providerLot.auction.endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            source = LotSource(
                name = providerLot.providerName,
                lotUrl = "https://example.com/lot/${providerLot.id}" // TODO: Generate proper URL
            ),
            location = LotLocation(
                city = providerLot.location.city,
                state = providerLot.location.state,
                country = providerLot.location.country
            ),
            fees = LotFees(
                buyerPremiumPercent = providerLot.metadata.fees
                    .find { it.type == FeeType.BUYER_PREMIUM }
                    ?.amount,
                documentationFee = providerLot.metadata.fees
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
