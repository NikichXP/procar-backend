package com.procar.customer.ui.navigation

sealed class Screen {
    data object Feed : Screen()
    data class LotDetail(val lotId: String) : Screen()
    data object Profile : Screen()
}
