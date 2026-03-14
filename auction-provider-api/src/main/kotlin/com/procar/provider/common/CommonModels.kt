package com.procar.provider.common

import java.time.LocalDateTime

data class PriceRange(
    val min: Double?,
    val max: Double?
)

data class LocalDateTimeRange(
    val start: LocalDateTime?,
    val end: LocalDateTime?
)

data class GeoCoordinates(
    val latitude: Double,
    val longitude: Double
)

data class PaginationResponse(
    val hasNext: Boolean,
    val nextCursor: String?
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

enum class SearchType {
    STANDARD, FUZZY, SEMANTIC, HYBRID
}

enum class AlertSeverity {
    INFO, WARNING, URGENT, CRITICAL
}

enum class ActivityType {
    VIEW, BID, WATCH, SHARE, QUESTION, ALERT
}
