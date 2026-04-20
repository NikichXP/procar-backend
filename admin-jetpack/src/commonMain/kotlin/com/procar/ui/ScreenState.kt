package com.procar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
class ScreenState {
    var loading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var showCreateDialog by mutableStateOf(false)
}

@Composable
fun rememberScreenState(): ScreenState = remember { ScreenState() }

fun CoroutineScope.launchWithState(
    state: ScreenState,
    errorPrefix: String,
    block: suspend () -> Unit,
) {
    launch {
        state.loading = true
        state.errorMessage = null
        try {
            block()
        } catch (e: Exception) {
            state.errorMessage = "$errorPrefix: ${e.message}"
        } finally {
            state.loading = false
        }
    }
}
