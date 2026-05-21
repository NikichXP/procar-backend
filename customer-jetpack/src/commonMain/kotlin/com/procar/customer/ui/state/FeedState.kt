package com.procar.customer.ui.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.procar.gateway.api.dto.CarCondition
import com.procar.gateway.api.dto.LotSummary

@Stable
class FeedState {
    val lots = mutableStateListOf<LotSummary>()
    var isLoading by mutableStateOf(false)
    var isLoadingMore by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var nextCursor by mutableStateOf<String?>(null)
    var hasMore by mutableStateOf(true)

    fun reset() {
        lots.clear()
        nextCursor = null
        hasMore = true
        errorMessage = null
    }
}

@Stable
class FilterState {
    var condition by mutableStateOf<CarCondition?>(null)
    var sort by mutableStateOf("endTime,asc")
    var priceFrom by mutableStateOf("")
    var priceTo by mutableStateOf("")

    val priceFromDouble: Double? get() = priceFrom.toDoubleOrNull()
    val priceToDouble: Double? get() = priceTo.toDoubleOrNull()
}
