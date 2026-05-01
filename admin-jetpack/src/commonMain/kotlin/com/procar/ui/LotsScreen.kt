package com.procar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.procar.api.fetchLots
import com.procar.model.AdminLotResponse
import kotlinx.coroutines.launch

@Composable
fun LotsScreen() {
    val scope = rememberCoroutineScope()
    val state = rememberScreenState()
    var lots by remember { mutableStateOf<List<AdminLotResponse>>(emptyList()) }
    var nextCursor by remember { mutableStateOf<String?>(null) }
    var hasNext by remember { mutableStateOf(false) }
    var loadingMore by remember { mutableStateOf(false) }
    var selectedLot by remember { mutableStateOf<AdminLotResponse?>(null) }

    fun loadLots() {
        scope.launchWithState(state, "Error loading lots") {
            val page = fetchLots()
            lots = page.lots
            nextCursor = page.pagination.nextCursor
            hasNext = page.pagination.hasNext
        }
    }

    fun loadMore() {
        val cursor = nextCursor ?: return
        if (loadingMore) return
        scope.launch {
            loadingMore = true
            state.errorMessage = null
            try {
                val page = fetchLots(cursor = cursor)
                lots = lots + page.lots
                nextCursor = page.pagination.nextCursor
                hasNext = page.pagination.hasNext
            } catch (e: Exception) {
                state.errorMessage = "Error loading more lots: ${e.message}"
            } finally {
                loadingMore = false
            }
        }
    }

    LaunchedEffect(Unit) { loadLots() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Lots", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { state.showCreateDialog = true }) { Text("Create Lot") }
            OutlinedButton(onClick = { loadLots() }) { Text("Refresh") }
        }

        Spacer(Modifier.height(8.dp))
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (state.loading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            DataTable(
                headers = listOf("ID", "Title", "Status", "Current Bid", "Actions"),
                rows = lots,
                rowKey = { it.id },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                cellContent = { lot, col ->
                    when (col) {
                        0 -> Text(lot.id.take(8) + "…", style = MaterialTheme.typography.bodySmall)
                        1 -> Text(lot.title)
                        2 -> Text(lot.status.name)
                        3 -> Text(lot.auction?.currentBid?.let { "$$it" } ?: "N/A")
                        4 -> TextButton(onClick = { selectedLot = lot }) { Text("Details") }
                    }
                }
            )

            if (hasNext) {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    OutlinedButton(
                        onClick = { loadMore() },
                        enabled = !loadingMore,
                    ) {
                        if (loadingMore) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Load more")
                        }
                    }
                }
            }
        }
    }

    if (state.showCreateDialog) {
        CreateLotDialog(
            onDismiss = { state.showCreateDialog = false },
            onCreated = {
                state.showCreateDialog = false
                loadLots()
            }
        )
    }

    selectedLot?.let { lot ->
        LotDetailsDialog(
            lot = lot,
            onDismiss = { selectedLot = null },
            onSaved = { loadLots() }
        )
    }
}
