package com.procar.core.service

import com.procar.gateway.api.dto.*
import com.procar.gateway.api.dto.LotStatus
import com.procar.gateway.api.dto.LotType
import com.procar.provider.InternalLotAPI
import com.procar.provider.common.ApiResponse
import com.procar.provider.common.PaginationRequest
import com.procar.provider.common.PriceRange
import com.procar.provider.lot.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter
import com.procar.provider.lot.LotBrand as ProviderLotBrand
import com.procar.provider.lot.LotStatus as ProviderLotStatus
import com.procar.provider.lot.LotType as ProviderLotType
import com.procar.provider.lot.VehicleCondition as ProviderVehicleCondition

@Service
class LotService(
    @Qualifier("internalLotHttpClient") private val internalLotAPI: InternalLotAPI
) {

    suspend fun getLots(request: LotSearchRequest): LotPage {
        val advancedRequest = AdvancedLotSearchRequest(
            query = null,
            filters = LotSearchFilters(
                status = request.statuses.map { mapStatus(it) },
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
                cursor = request.cursor
            )
        )
        
        val response: ResponseEntity<ApiResponse<LotSearchResponse>> = internalLotAPI.searchLots(advancedRequest)
        val apiResponse = response.body ?: throw RuntimeException("Failed to search lots")
        val searchResult = apiResponse.data
        
        // Convert internal response to gateway DTO
        val lots = searchResult.results.map { vehicleLot ->
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
                    images = vehicleLot.vehicle.images.map { it.url }
                ),
                status = mapStatusToGateway(vehicleLot.status),
                currentBid = vehicleLot.auction?.currentBid,
                startingBid = vehicleLot.auction?.startingBid,
                bidStep = vehicleLot.auction?.bidIncrement,
                bidsCount = vehicleLot.auction?.totalBids,
                startTime = vehicleLot.auction?.startTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                endTime = vehicleLot.auction?.endTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                photos = vehicleLot.vehicle.images.map { it.url },
                lotType = mapLotType(vehicleLot.lotType),
                buyoutPrice = vehicleLot.buyoutPrice,
                brand = mapBrand(vehicleLot.brand),
            )
        }

        return LotPage(
            data = lots,
            nextCursor = searchResult.nextCursor,
            limit = request.limit
        )
    }

    suspend fun countLots(request: LotSearchRequest): Int {
        // This is no longer used in cursor-based pagination
        return 0
    }

    suspend fun getLotDetail(lotId: String): LotDetail {
        val response: ResponseEntity<ApiResponse<VehicleLot>> = internalLotAPI.getLotDetail(lotId)
        val apiResponse = response.body ?: throw RuntimeException("Failed to get lot detail")
        val vehicleLot = apiResponse.data
        
        // Convert internal response to gateway DTO
        return LotDetail(
            id = vehicleLot.id,
            car = mapCarInfo(vehicleLot),
            status = mapStatusToGateway(vehicleLot.status),
            currentBid = vehicleLot.auction?.currentBid,
            startingBid = vehicleLot.auction?.startingBid,
            bidStep = vehicleLot.auction?.bidIncrement,
            bidsCount = vehicleLot.auction?.totalBids,
            startTime = vehicleLot.auction?.startTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            endTime = vehicleLot.auction?.endTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            photos = vehicleLot.vehicle.images.map { it.url },
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
            recentBids = emptyList(), // TODO: Fetch recent bids from bid API
            lotType = mapLotType(vehicleLot.lotType),
            buyoutPrice = vehicleLot.buyoutPrice,
            brand = mapBrand(vehicleLot.brand),
        )
    }

    suspend fun getLotSummary(lotId: String): LotSummary {
        val response: ResponseEntity<ApiResponse<VehicleLot>> = internalLotAPI.getLotDetail(lotId)
        val apiResponse = response.body ?: throw RuntimeException("Failed to get lot summary")
        val vehicleLot = apiResponse.data

        return LotSummary(
            id = vehicleLot.id,
            car = mapCarInfo(vehicleLot),
            status = mapStatusToGateway(vehicleLot.status),
            currentBid = vehicleLot.auction?.currentBid,
            startingBid = vehicleLot.auction?.startingBid,
            bidStep = vehicleLot.auction?.bidIncrement,
            bidsCount = vehicleLot.auction?.totalBids,
            startTime = vehicleLot.auction?.startTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            endTime = vehicleLot.auction?.endTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            photos = vehicleLot.vehicle.images.map { it.url },
            lotType = mapLotType(vehicleLot.lotType),
            buyoutPrice = vehicleLot.buyoutPrice,
            brand = mapBrand(vehicleLot.brand),
        )
    }

    private fun mapCarInfo(vehicleLot: VehicleLot): CarInfo {
        return CarInfo(
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
                titleState = null
            )
        )
    }

    private fun mapStatus(status: LotStatus): ProviderLotStatus {
        return when (status) {
            LotStatus.PENDING -> ProviderLotStatus.PENDING
            LotStatus.ACTIVE -> ProviderLotStatus.ACTIVE
            LotStatus.AWAIT_SELLER_CONFIRMATION -> ProviderLotStatus.AWAIT_SELLER_CONFIRMATION
            LotStatus.AWAITING_PAYMENT -> ProviderLotStatus.AWAITING_PAYMENT
            LotStatus.AWAITING_SHIPMENT -> ProviderLotStatus.AWAITING_SHIPMENT
            LotStatus.IN_TRANSIT -> ProviderLotStatus.IN_TRANSIT
            LotStatus.COMPLETED -> ProviderLotStatus.COMPLETED
            LotStatus.INVALID -> ProviderLotStatus.DRAFT
        }
    }

    private fun mapStatusToGateway(status: ProviderLotStatus): LotStatus {
        return when (status) {
            ProviderLotStatus.DRAFT -> LotStatus.INVALID
            ProviderLotStatus.PENDING -> LotStatus.PENDING
            ProviderLotStatus.ACTIVE -> LotStatus.ACTIVE
            ProviderLotStatus.AWAIT_SELLER_CONFIRMATION -> LotStatus.AWAIT_SELLER_CONFIRMATION
            ProviderLotStatus.AWAITING_PAYMENT -> LotStatus.AWAITING_PAYMENT
            ProviderLotStatus.AWAITING_SHIPMENT -> LotStatus.AWAITING_SHIPMENT
            ProviderLotStatus.IN_TRANSIT -> LotStatus.IN_TRANSIT
            ProviderLotStatus.COMPLETED -> LotStatus.COMPLETED
            ProviderLotStatus.HIDDEN -> LotStatus.INVALID
        }
    }

    private fun mapCondition(condition: ProviderVehicleCondition): CarCondition {
        return when (condition) {
            ProviderVehicleCondition.EXCELLENT,
            ProviderVehicleCondition.GOOD -> CarCondition.USED
            ProviderVehicleCondition.FAIR,
            ProviderVehicleCondition.POOR -> CarCondition.USED
            ProviderVehicleCondition.DAMAGED,
            ProviderVehicleCondition.SALVAGE -> CarCondition.DAMAGED
        }
    }

    private fun mapLotType(t: ProviderLotType): LotType = when (t) {
        ProviderLotType.AUCTION -> LotType.AUCTION
        ProviderLotType.BUYOUT  -> LotType.BUYOUT
        ProviderLotType.HYBRID  -> LotType.HYBRID
    }

    private fun mapBrand(b: ProviderLotBrand): LotBrand = when (b) {
        ProviderLotBrand.PARTNER -> LotBrand.PARTNER
        ProviderLotBrand.SELECT -> LotBrand.SELECT
    }
}
