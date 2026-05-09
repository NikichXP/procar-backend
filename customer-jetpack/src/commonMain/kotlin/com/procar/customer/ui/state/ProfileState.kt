package com.procar.customer.ui.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.procar.gateway.api.dto.LotSummary
import com.procar.gateway.api.dto.UserBid
import com.procar.gateway.api.dto.UserInfoDto

@Stable
class ProfileState {
    var user by mutableStateOf<UserInfoDto?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    val bids = mutableStateListOf<UserBid>()
    var bidsLoading by mutableStateOf(false)
    var bidsError by mutableStateOf<String?>(null)

    val watchlist = mutableStateListOf<LotSummary>()
    var watchlistLoading by mutableStateOf(false)
    var watchlistError by mutableStateOf<String?>(null)

    var selectedTab by mutableStateOf(ProfileTab.PROFILE)

    fun reset() {
        user = null
        errorMessage = null
        bids.clear()
        bidsError = null
        watchlist.clear()
        watchlistError = null
    }
}

enum class ProfileTab {
    PROFILE, BIDS, WATCHLIST
}
