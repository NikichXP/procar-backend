package com.procar.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.procar.api.updateLot
import com.procar.model.AdminLotResponse
import com.procar.ui.components.SectionEditState
import com.procar.ui.lot.AuctionSection
import com.procar.ui.lot.GeneralSection
import com.procar.ui.lot.LocationSection
import com.procar.ui.lot.PhotosSection
import com.procar.ui.lot.VehicleSection
import com.procar.ui.state.rememberLotEditState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun LotDetailsDialog(
    lot: AdminLotResponse,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val editState = rememberLotEditState(lot)
    var error by remember { mutableStateOf<String?>(null) }
    var savingSection by remember { mutableStateOf<String?>(null) }

    fun trySave(name: String, section: SectionEditState) {
        val validationError = section.validate()
        if (validationError != null) {
            error = validationError
            return
        }
        scope.launchSave(
            beforeStart = { savingSection = name; error = null },
            onFinish = { savingSection = null },
            onError = { error = "Error updating $name: ${it.message}" },
        ) {
            updateLot(lot.id, section.buildRequest())
            section.commit()
            onSaved()
        }
    }

    AlertDialog(
        onDismissRequest = { if (!editState.isAnyModified && savingSection == null) onDismiss() },
        title = { Text(lot.title.ifBlank { "Lot Details" }) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).width(520.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                GeneralSection(
                    lot = lot,
                    state = editState.general,
                    saving = savingSection == "General",
                    onSave = { trySave("General", editState.general) },
                )
                VehicleSection(
                    state = editState.vehicle,
                    saving = savingSection == "Vehicle",
                    onSave = { trySave("Vehicle", editState.vehicle) },
                )
                if (editState.auction != null) {
                    AuctionSection(
                        lot = lot,
                        state = editState.auction,
                        lotType = editState.general.lotType.current,
                        saving = savingSection == "Auction",
                        onSave = { trySave("Auction", editState.auction) },
                    )
                } else {
                    AuctionSection(
                        lot = lot,
                        state = null,
                        lotType = editState.general.lotType.current,
                        saving = false,
                        onSave = {},
                    )
                }
                LocationSection(
                    state = editState.location,
                    saving = savingSection == "Location",
                    onSave = { trySave("Location", editState.location) },
                )
                PhotosSection(
                    lot = lot,
                    onUploaded = onSaved,
                )
            }
        },
        confirmButton = {
            Button(enabled = savingSection == null, onClick = onDismiss) {
                Text(if (editState.isAnyModified) "Close (discard changes)" else "Close")
            }
        },
    )
}

/** Helper that runs a suspending block with standard before/after/error bookkeeping. */
private inline fun CoroutineScope.launchSave(
    crossinline beforeStart: () -> Unit,
    crossinline onFinish: () -> Unit,
    crossinline onError: (Throwable) -> Unit,
    crossinline block: suspend () -> Unit,
) {
    beforeStart()
    launch {
        try {
            block()
        } catch (e: Exception) {
            onError(e)
        } finally {
            onFinish()
        }
    }
}
