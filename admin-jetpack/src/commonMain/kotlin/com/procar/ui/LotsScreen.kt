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
    var lots by remember { mutableStateOf<List<AdminLotResponse>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedLot by remember { mutableStateOf<AdminLotResponse?>(null) }

    fun loadLots() {
        scope.launch {
            loading = true
            errorMessage = null
            try {
                lots = fetchLots()
            } catch (e: Exception) {
                errorMessage = "Error loading lots: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadLots() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Lots", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showCreateDialog = true }) { Text("Create Lot") }
            OutlinedButton(onClick = { loadLots() }) { Text("Refresh") }
        }

        Spacer(Modifier.height(8.dp))
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (loading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            DataTable(
                headers = listOf("ID", "Title", "Status", "Current Bid", "Actions"),
                rows = lots,
                rowKey = { it.id },
                cellContent = { lot, col ->
                    when (col) {
                        0 -> Text(lot.id.take(8) + "…", style = MaterialTheme.typography.bodySmall)
                        1 -> Text(lot.title)
                        2 -> Text(lot.status)
                        3 -> Text("$${lot.auction.currentBid}")
                        4 -> TextButton(onClick = { selectedLot = lot }) { Text("Details") }
                    }
                }
            )
        }
    }

    if (showCreateDialog) {
        CreateLotDialog(
            onDismiss = { showCreateDialog = false },
            onCreated = {
                showCreateDialog = false
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
