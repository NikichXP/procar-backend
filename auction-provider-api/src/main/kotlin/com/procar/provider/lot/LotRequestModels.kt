package com.procar.provider.lot

import com.procar.provider.common.*
import java.time.LocalDateTime

data class AdvancedLotSearchRequest(
    val query: String?,
    val filters: LotSearchFilters,
    val sorting: List<SortCriteria>,
    val pagination: PaginationRequest,
    val searchType: SearchType = SearchType.STANDARD,
    val fuzzyMatching: Boolean = true,
    val semanticSearch: Boolean = false
)

data class LotSearchFilters(
    val providers: List<String>?,
    val status: List<LotStatus>?,
    val priceRange: PriceRange?,
    val yearRange: IntRange?,
    val mileageRange: IntRange?,
    val vehicle: VehicleFilters,
    val auction: AuctionFilters,
    val location: LocationFilters,
    val seller: SellerFilters,
    val tags: List<String>?,
    val categories: List<String>?,
    val conditions: List<VehicleCondition>?,
    val titleStatus: List<TitleStatus>?
)

data class VehicleFilters(
    val makes: List<String>?,
    val models: List<String>?,
    val bodyTypes: List<String>?,
    val fuelTypes: List<FuelType>?,
    val transmissions: List<TransmissionType>?,
    val drivetrains: List<DrivetrainType>?,
    val colors: List<String>?,
    val features: List<String>?,
    val hasDamage: Boolean?,
    val hasInspection: Boolean?
)

data class AuctionFilters(
    val auctionTypes: List<AuctionType>?,
    val endTimeRange: LocalDateTimeRange?,
    val bidCountRange: IntRange?,
    val hasReserve: Boolean?,
    val hasBuyItNow: Boolean?
)

data class LocationFilters(
    val states: List<String>?,
    val cities: List<String>?,
    val zipCodes: List<String>?,
    val radius: LocationRadius?
)

data class SellerFilters(
    val sellerTypes: List<SellerType>?,
    val minRating: Double?,
    val minTotalSales: Int?
)

data class LocationRadius(
    val latitude: Double,
    val longitude: Double,
    val radiusMiles: Int
)
