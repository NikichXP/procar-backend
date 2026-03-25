package com.procar.provider.common

data class PriceRange(
    val min: Double? = null,
    val max: Double? = null
)


data class GeoCoordinates(
    val latitude: Double,
    val longitude: Double
)

data class PaginationResponse(
    val hasNext: Boolean,
    val nextCursor: String? = null
)

data class PaginationRequest(
    val cursor: String? = null,
    val limit: Int = 20
)

