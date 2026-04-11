package com.procar

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.procar.ui.AdminApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Procar Admin",
        state = rememberWindowState(width = 1200.dp, height = 800.dp),
    ) {
        AdminApp()
    }
}
