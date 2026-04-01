package com.procar

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.procar.ui.AdminApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow("Procar Admin") {
        AdminApp()
    }
}
