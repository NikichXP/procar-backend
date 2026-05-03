package com.procar.customer

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.procar.customer.ui.CustomerApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Procar",
        state = rememberWindowState(width = 420.dp, height = 860.dp),
    ) {
        CustomerApp()
    }
}
