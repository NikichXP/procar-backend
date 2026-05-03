package com.procar.customer

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.procar.customer.ui.CustomerApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow("Procar") {
        CustomerApp()
    }
}
