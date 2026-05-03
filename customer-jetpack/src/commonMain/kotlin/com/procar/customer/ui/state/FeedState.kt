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
    var currentPage by mutableStateOf(0)
    var totalPages by mutableStateOf(1)

    val hasMore: Boolean get() = currentPage + 1 < totalPages

    fun reset() {
        lots.clear()
        currentPage = 0
        totalPages = 1
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
