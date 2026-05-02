package com.procar.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.procar.api.createLot
import com.procar.api.fetchBrokers
import com.procar.api.fetchWarehouses
import com.procar.api.uploadLotPhoto
import com.procar.gateway.api.dto.*
import com.procar.ui.components.EnumDropdown
import com.procar.ui.components.SectionHeader
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch

@Composable
fun CreateLotDialog(onDismiss: () -> Unit, onCreated: () -> Unit) {
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var brokers by remember { mutableStateOf<List<BrokerDto>>(emptyList()) }
    var selectedBrokerId by remember { mutableStateOf<String?>(null) }
    var loadingBrokers by remember { mutableStateOf(false) }
    var warehouses by remember { mutableStateOf<List<AdminWarehouseResponse>>(emptyList()) }
    var selectedWarehouseId by remember { mutableStateOf<String?>(null) }
    var loadingWarehouses by remember { mutableStateOf(false) }

    // Photo upload state
    val selectedPhotos = remember { mutableStateListOf<io.github.vinceglb.filekit.core.PlatformFile>() }
    var uploadingPhotos by remember { mutableStateOf(false) }
    var photoUploadProgress by remember { mutableStateOf<String?>(null) }

    // Load brokers when dialog opens
    LaunchedEffect(Unit) {
        loadingBrokers = true
        loadingWarehouses = true
        try {
            brokers = fetchBrokers()
            warehouses = fetchWarehouses()
        } catch (e: Exception) {
            error = "Error loading data: ${e.message}"
        } finally {
            loadingBrokers = false
            loadingWarehouses = false
        }
    }

    // Vehicle fields
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var vin by remember { mutableStateOf("") }
    var bodyType by remember { mutableStateOf(BodyType.SEDAN) }
    var transmission by remember { mutableStateOf(Transmission.AUTOMATIC) }
    var drivetrain by remember { mutableStateOf(Drivetrain.FWD) }
    var fuelType by remember { mutableStateOf(FuelType.GASOLINE) }
    var condition by remember { mutableStateOf(VehicleCondition.GOOD) }
    var engineType by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }

    // Lot type
    var lotType by remember { mutableStateOf(LotType.AUCTION) }
    var buyoutPrice by remember { mutableStateOf("") }

    // Auction fields
    var startingBid by remember { mutableStateOf("") }
    var bidIncrement by remember { mutableStateOf("100") }

    // Create a mapping of broker display names to broker objects
    val brokerOptions = brokers.associateBy { "${it.name} (${it.id})" }
    var selectedBrokerDisplay by remember { mutableStateOf("") }

    // Create a mapping of warehouse display names to warehouse objects
    val warehouseOptions = warehouses.associateBy { "${it.name} (${it.id})" }
    var selectedWarehouseDisplay by remember { mutableStateOf("") }

    LaunchedEffect(selectedBrokerDisplay) {
        selectedBrokerId = brokerOptions[selectedBrokerDisplay]?.id
    }

    LaunchedEffect(selectedWarehouseDisplay) {
        selectedWarehouseId = warehouseOptions[selectedWarehouseDisplay]?.id
    }

    // File picker for photos
    val photoPicker = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Multiple(),
        title = "Select photos",
    ) { files ->
        if (files.isNullOrEmpty()) return@rememberFilePickerLauncher
        selectedPhotos.addAll(files)
    }

    fun validate(): String? {
        if (make.isBlank()) return "Make is required"
        if (model.isBlank()) return "Model is required"
        if (year.toIntOrNull() == null) return "Valid year is required"
        if (engineType.isBlank()) return "Cylinder count/shape is required"
        if (lotType != LotType.BUYOUT) {
            if (startingBid.toDoubleOrNull() == null) return "Valid starting bid is required"
            if (bidIncrement.toDoubleOrNull() == null) return "Valid bid increment is required"
        }
        if (lotType != LotType.AUCTION) {
            if (buyoutPrice.toDoubleOrNull() == null) return "Valid buyout price is required"
        }
        if (selectedWarehouseId.isNullOrBlank()) return "Warehouse is required"
        if (selectedBrokerId.isNullOrBlank()) return "Broker is required"
        return null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Lot") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).width(500.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                SectionHeader("Lot Type")
                EnumDropdown(
                    "Lot Type", lotType.name,
                    LotType.entries.map { it.name },
                ) { lotType = LotType.valueOf(it) }

                SectionHeader("Vehicle Details")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(make, { make = it.replace("\t", "") }, label = { Text("Make *") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(model, { model = it.replace("\t", "") }, label = { Text("Model *") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(year, { year = it.replace("\t", "") }, label = { Text("Year *") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(vin, { vin = it.replace("\t", "") }, label = { Text("VIN") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(engineType, { engineType = it.replace("\t", "") }, label = { Text("Cylinder count/shape *") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(mileage, { mileage = it.replace("\t", "") }, label = { Text("Mileage") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                EnumDropdown("Body Type", bodyType.displayName, BodyType.entries.map { it.displayName }) { selected -> bodyType = BodyType.entries.first { it.displayName == selected } }
                EnumDropdown("Transmission", transmission.displayName, Transmission.entries.map { it.displayName }) { selected -> transmission = Transmission.entries.first { it.displayName == selected } }
                EnumDropdown("Drivetrain", drivetrain.displayName, Drivetrain.entries.map { it.displayName }) { selected -> drivetrain = Drivetrain.entries.first { it.displayName == selected } }
                EnumDropdown("Fuel Type", fuelType.displayName, FuelType.entries.map { it.displayName }) { selected -> fuelType = FuelType.entries.first { it.displayName == selected } }
                EnumDropdown("Condition", condition.displayName, VehicleCondition.entries.map { it.displayName }) { selected -> condition = VehicleCondition.entries.first { it.displayName == selected } }

                if (lotType != LotType.BUYOUT) {
                    SectionHeader("Auction Details")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(startingBid, { startingBid = it.replace("\t", "") }, label = { Text("Starting Bid *") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(bidIncrement, { bidIncrement = it.replace("\t", "") }, label = { Text("Bid Increment *") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                }
                if (lotType != LotType.AUCTION) {
                    SectionHeader("Buyout")
                    OutlinedTextField(buyoutPrice, { buyoutPrice = it.replace("\t", "") }, label = { Text("Buyout Price *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }

                SectionHeader("Location")
                if (loadingWarehouses) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else {
                    EnumDropdown(
                        "Warehouse *",
                        selectedWarehouseDisplay,
                        warehouseOptions.keys.toList()
                    ) { selectedWarehouseDisplay = it }
                }

                SectionHeader("Seller")
                if (loadingBrokers) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else {
                    EnumDropdown(
                        "Broker *",
                        selectedBrokerDisplay,
                        brokerOptions.keys.toList()
                    ) { selectedBrokerDisplay = it }
                }

                SectionHeader("Photos")
                if (selectedPhotos.isEmpty()) {
                    Text("No photos selected", style = MaterialTheme.typography.bodySmall)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        selectedPhotos.forEach { file ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(file.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                OutlinedButton(
                                    onClick = { selectedPhotos.remove(file) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("×", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { photoPicker.launch() }) {
                        Text("Add Photos")
                    }
                }
                photoUploadProgress?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(
                enabled = !saving,
                onClick = {
                    val validationError = validate()
                    if (validationError != null) {
                        error = validationError
                        return@Button
                    }
                    scope.launch {
                        saving = true
                        try {
                            val autoTitle = "${year} ${make} ${model}".trim()
                            val createdLot = createLot(AdminCreateLotRequest(
                                brokerId = selectedBrokerId!!,
                                warehouseId = selectedWarehouseId!!,
                                externalId = autoTitle.replace(" ", "_").lowercase(),
                                title = autoTitle,
                                description = "",
                                status = AdminLotStatus.DRAFT,
                                vehicle = AdminVehicleInfoRequest(
                                    make = make, model = model,
                                    year = year.toInt(),
                                    bodyType = bodyType.toString(),
                                    transmission = transmission.toString(),
                                    drivetrain = drivetrain.toString(),
                                    fuelType = fuelType.toString(),
                                    condition = condition.toString(),
                                    engine = AdminEngineInfoRequest(type = engineType),
                                    vin = vin.takeIf { it.isNotBlank() },
                                    mileage = mileage.toIntOrNull(),
                                ),
                                lotType = lotType,
                                auction = if (lotType == LotType.BUYOUT) null else AdminAuctionInfoRequest(
                                    currentBid = startingBid.toDouble(),
                                    startingBid = startingBid.toDouble(),
                                    bidIncrement = bidIncrement.toDouble(),
                                    startTime = "2025-01-01T00:00:00",
                                    endTime = "2025-12-31T00:00:00",
                                ),
                                buyoutPrice = if (lotType == LotType.AUCTION) null else buyoutPrice.toDoubleOrNull(),
                            ))

                            // Upload photos if any were selected
                            if (selectedPhotos.isNotEmpty()) {
                                uploadingPhotos = true
                                selectedPhotos.forEachIndexed { idx, file ->
                                    photoUploadProgress = "Uploading photo ${idx + 1}/${selectedPhotos.size}: ${file.name}"
                                    val bytes = file.readBytes()
                                    uploadLotPhoto(
                                        lotId = createdLot.id,
                                        fileName = file.name,
                                        contentType = guessImageContentType(file.name),
                                        bytes = bytes,
                                    )
                                }
                                photoUploadProgress = null
                                uploadingPhotos = false
                            }

                            onCreated()
                        } catch (e: Exception) {
                            error = "Error: ${e.message}"
                        } finally {
                            saving = false
                            uploadingPhotos = false
                        }
                    }
                }
            ) {
                if (saving) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                else Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/** Best-effort content-type from filename extension. */
private fun guessImageContentType(fileName: String): String {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "bmp" -> "image/bmp"
        "heic" -> "image/heic"
        "heif" -> "image/heif"
        "svg" -> "image/svg+xml"
        else -> "application/octet-stream"
    }
}
