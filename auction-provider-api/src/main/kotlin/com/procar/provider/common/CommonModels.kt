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
    val size: Int = 20,
    val cursor: String? = null
)

data class SortCriteria(
    val field: SortField,
    val direction: SortDirection
)

enum class SortDirection {
    ASC, DESC
}

enum class SortField {
    END_TIME, START_TIME, CURRENT_BID, STARTING_BID, MILEAGE, YEAR, MAKE, MODEL, LOCATION, RATING, AMOUNT, PLACED_AT
}
