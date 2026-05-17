package com.procar.provider.lot

import com.procar.provider.common.PaginationRequest
import com.procar.provider.common.PriceRange

data class AdvancedLotSearchRequest(
    val query: String? = null,
    val filters: LotSearchFilters? = null,
    val pagination: PaginationRequest = PaginationRequest()
)

data class LotSearchFilters(
    val brand: LotBrand? = null,
    val providers: List<String>? = null,
    val status: List<LotStatus>? = null,
    val priceRange: PriceRange? = null,
    val yearRange: IntRange? = null,
    val mileageRange: IntRange? = null,
    val vehicle: VehicleFilters? = null,
    val location: LocationFilters? = null,
    val tags: List<String>? = null,
    val categories: List<String>? = null,
    val conditions: List<VehicleCondition>? = null,
    val titleStatus: List<TitleStatus>? = null
)

data class VehicleFilters(
    val makes: List<String>? = null,
    val models: List<String>? = null,
    val bodyTypes: List<String>? = null,
    val fuelTypes: List<FuelType>? = null,
    val transmissions: List<TransmissionType>? = null,
    val drivetrains: List<DrivetrainType>? = null,
    val colors: List<String>? = null,
    val features: List<String>? = null,
    val hasDamage: Boolean? = null,
    val hasInspection: Boolean? = null
)


data class LocationFilters(
    val states: List<String>? = null,
    val cities: List<String>? = null,
    val zipCodes: List<String>? = null
)
