package com.procar.provider.lot

import com.procar.provider.common.*
import java.time.LocalDateTime

data class LotSearchResponse(
    val results: List<ProviderLot>,
    val pagination: PaginationResponse,
    val suggestions: List<String>?,
    val correctedQuery: String?
)

data class LotMonitoringResponse(
    val lotId: String,
    val status: LotStatus,
    val currentBid: Double,
    val timeRemaining: Long,
    val recentActivity: List<LotActivity>,
    val streamUrl: String?,
    val alerts: List<LotAlert>
)

data class LotActivity(
    val timestamp: LocalDateTime,
    val type: ActivityType,
    val description: String,
    val data: Map<String, Any>?
)

data class LotAlert(
    val type: AlertType,
    val message: String,
    val severity: AlertSeverity,
    val timestamp: LocalDateTime
)

enum class AlertType {
    PRICE_CHANGE, NEW_BID, ENDING_SOON, RESERVE_MET, STATUS_CHANGE
}
