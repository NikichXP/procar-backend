package com.procar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.procar.api.createLot
import com.procar.api.fetchLots
import com.procar.api.updateLot
import com.procar.model.*
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
            onSaved = {
                selectedLot = null
                loadLots()
            }
        )
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        HorizontalDivider(Modifier.padding(vertical = 2.dp))
        content()
    }
}

@Composable
private fun DetailRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("$label:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(140.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun LotDetailsDialog(lot: AdminLotResponse, onDismiss: () -> Unit, onSaved: () -> Unit) {
    val scope = rememberCoroutineScope()
    var isEditing by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Editable state, initialized from the lot
    var title by remember(lot.id) { mutableStateOf(lot.title) }
    var description by remember(lot.id) { mutableStateOf(lot.description) }
    var status by remember(lot.id) { mutableStateOf(lot.status) }

    // Vehicle
    var make by remember(lot.id) { mutableStateOf(lot.vehicle.make) }
    var model by remember(lot.id) { mutableStateOf(lot.vehicle.model) }
    var year by remember(lot.id) { mutableStateOf(lot.vehicle.year.toString()) }
    var vin by remember(lot.id) { mutableStateOf(lot.vehicle.vin ?: "") }
    var trim by remember(lot.id) { mutableStateOf(lot.vehicle.trim ?: "") }
    var color by remember(lot.id) { mutableStateOf(lot.vehicle.color ?: "") }
    var mileage by remember(lot.id) { mutableStateOf(lot.vehicle.mileage?.toString() ?: "") }
    var bodyType by remember(lot.id) { mutableStateOf(lot.vehicle.bodyType) }
    var transmission by remember(lot.id) { mutableStateOf(lot.vehicle.transmission) }
    var drivetrain by remember(lot.id) { mutableStateOf(lot.vehicle.drivetrain ?: "FWD") }
    var fuelType by remember(lot.id) { mutableStateOf(lot.vehicle.fuelType) }
    var condition by remember(lot.id) { mutableStateOf(lot.vehicle.condition) }
    var engineType by remember(lot.id) { mutableStateOf(lot.vehicle.engine?.type ?: "") }

    // Auction (currentBid is NOT editable)
    var startingBid by remember(lot.id) { mutableStateOf(lot.auction.startingBid.toString()) }
    var bidIncrement by remember(lot.id) { mutableStateOf(lot.auction.bidIncrement.toString()) }
    var auctionType by remember(lot.id) { mutableStateOf(lot.auction.auctionType) }
    var reservePrice by remember(lot.id) { mutableStateOf(lot.auction.reservePrice?.toString() ?: "") }
    var buyItNowPrice by remember(lot.id) { mutableStateOf(lot.auction.buyItNowPrice?.toString() ?: "") }
    var startTime by remember(lot.id) { mutableStateOf(lot.auction.startTime) }
    var endTime by remember(lot.id) { mutableStateOf(lot.auction.endTime) }

    // Location
    var address by remember(lot.id) { mutableStateOf(lot.location.address) }
    var city by remember(lot.id) { mutableStateOf(lot.location.city) }
    var state by remember(lot.id) { mutableStateOf(lot.location.state) }
    var zipCode by remember(lot.id) { mutableStateOf(lot.location.zipCode) }
    var country by remember(lot.id) { mutableStateOf(lot.location.country) }
    var timezone by remember(lot.id) { mutableStateOf(lot.location.timezone) }

    fun validate(): String? {
        if (title.isBlank()) return "Title is required"
        if (make.isBlank()) return "Make is required"
        if (model.isBlank()) return "Model is required"
        if (year.toIntOrNull() == null) return "Valid year is required"
        if (engineType.isBlank()) return "Engine type is required"
        if (startingBid.toDoubleOrNull() == null) return "Valid starting bid is required"
        if (bidIncrement.toDoubleOrNull() == null) return "Valid bid increment is required"
        if (address.isBlank()) return "Address is required"
        if (city.isBlank()) return "City is required"
        if (state.isBlank()) return "State is required"
        if (zipCode.isBlank()) return "ZIP code is required"
        if (country.isBlank()) return "Country is required"
        if (timezone.isBlank()) return "Timezone is required"
        return null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(lot.title.ifBlank { "Lot Details" }) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).width(520.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                if (!isEditing) {
                    DetailSection("General") {
                        DetailRow("ID", lot.id)
                        DetailRow("External ID", lot.externalId)
                        DetailRow("Title", lot.title)
                        DetailRow("Status", lot.status)
                        DetailRow("Description", lot.description)
                        DetailRow("Created", lot.createdAt)
                        DetailRow("Updated", lot.updatedAt)
                    }

                    DetailSection("Vehicle") {
                        val v = lot.vehicle
                        DetailRow("Make", v.make)
                        DetailRow("Model", v.model)
                        DetailRow("Year", v.year.toString())
                        DetailRow("VIN", v.vin)
                        DetailRow("Trim", v.trim)
                        DetailRow("Color", v.color)
                        DetailRow("Body Type", v.bodyType)
                        DetailRow("Transmission", v.transmission)
                        DetailRow("Fuel Type", v.fuelType)
                        DetailRow("Condition", v.condition)
                        DetailRow("Mileage", v.mileage?.toString())
                    }

                    DetailSection("Auction") {
                        val a = lot.auction
                        DetailRow("Type", a.auctionType)
                        DetailRow("Current Bid", "$${a.currentBid}")
                        DetailRow("Starting Bid", "$${a.startingBid}")
                        DetailRow("Bid Increment", "$${a.bidIncrement}")
                        DetailRow("Total Bids", a.totalBids.toString())
                        DetailRow("Reserve Price", a.reservePrice?.let { "$$it" })
                        DetailRow("Buy It Now", a.buyItNowPrice?.let { "$$it" })
                        DetailRow("Start Time", a.startTime)
                        DetailRow("End Time", a.endTime)
                    }

                    DetailSection("Location") {
                        val l = lot.location
                        DetailRow("Address", l.address)
                        DetailRow("City", l.city)
                        DetailRow("State", l.state)
                        DetailRow("ZIP", l.zipCode)
                        DetailRow("Country", l.country)
                        DetailRow("Timezone", l.timezone)
                    }
                } else {
                    DetailSection("General") {
                        DetailRow("ID", lot.id)
                        DetailRow("External ID", lot.externalId)
                        DetailRow("Created", lot.createdAt)
                        DetailRow("Updated", lot.updatedAt)
                        OutlinedTextField(title, { title = it }, label = { Text("Title *") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                        EnumDropdown("Status", status, listOf("DRAFT","PENDING","ACTIVE","SOLD","CANCELLED","EXPIRED","HIDDEN")) { status = it }
                    }

                    SectionHeader("Vehicle")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(make, { make = it }, label = { Text("Make *") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(model, { model = it }, label = { Text("Model *") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(year, { year = it }, label = { Text("Year *") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(vin, { vin = it }, label = { Text("VIN") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(trim, { trim = it }, label = { Text("Trim") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(color, { color = it }, label = { Text("Color") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(engineType, { engineType = it }, label = { Text("Engine Type *") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(mileage, { mileage = it }, label = { Text("Mileage") }, modifier = Modifier.weight(1f))
                    }
                    EnumDropdown("Body Type", bodyType, listOf("SEDAN","SUV","TRUCK","COUPE","CONVERTIBLE","HATCHBACK","WAGON","VAN","MINIVAN","OTHER")) { bodyType = it }
                    EnumDropdown("Transmission", transmission, listOf("AUTOMATIC","MANUAL","CVT","DCT")) { transmission = it }
                    EnumDropdown("Drivetrain", drivetrain, listOf("FWD","RWD","AWD","4WD")) { drivetrain = it }
                    EnumDropdown("Fuel Type", fuelType, listOf("GASOLINE","DIESEL","ELECTRIC","HYBRID","PLUG_IN_HYBRID")) { fuelType = it }
                    EnumDropdown("Condition", condition, listOf("NEW","USED","CERTIFIED","SALVAGE")) { condition = it }

                    SectionHeader("Auction")
                    DetailRow("Current Bid", "$${lot.auction.currentBid}")
                    DetailRow("Total Bids", lot.auction.totalBids.toString())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(startingBid, { startingBid = it }, label = { Text("Starting Bid *") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(bidIncrement, { bidIncrement = it }, label = { Text("Bid Increment *") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(reservePrice, { reservePrice = it }, label = { Text("Reserve Price") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(buyItNowPrice, { buyItNowPrice = it }, label = { Text("Buy It Now") }, modifier = Modifier.weight(1f))
                    }
                    EnumDropdown("Auction Type", auctionType, listOf("AUCTION","BUY_IT_NOW","HYBRID")) { auctionType = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(startTime, { startTime = it }, label = { Text("Start Time *") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(endTime, { endTime = it }, label = { Text("End Time *") }, modifier = Modifier.weight(1f))
                    }

                    SectionHeader("Location")
                    OutlinedTextField(address, { address = it }, label = { Text("Address *") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(city, { city = it }, label = { Text("City *") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(state, { state = it }, label = { Text("State *") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(zipCode, { zipCode = it }, label = { Text("ZIP *") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(country, { country = it }, label = { Text("Country *") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(timezone, { timezone = it }, label = { Text("Timezone *") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            if (!isEditing) {
                Button(onClick = { isEditing = true; error = null }) { Text("Edit") }
            } else {
                Button(
                    enabled = !saving,
                    onClick = {
                        val v = validate()
                        if (v != null) { error = v; return@Button }
                        scope.launch {
                            saving = true
                            error = null
                            try {
                                updateLot(
                                    lot.id,
                                    AdminUpdateLotRequest(
                                        title = title,
                                        description = description,
                                        status = status,
                                        vehicle = AdminVehicleInfoRequest(
                                            make = make,
                                            model = model,
                                            year = year.toInt(),
                                            bodyType = bodyType,
                                            transmission = transmission,
                                            drivetrain = drivetrain,
                                            fuelType = fuelType,
                                            condition = condition,
                                            engine = AdminEngineInfoRequest(type = engineType),
                                            vin = vin.takeIf { it.isNotBlank() },
                                            trim = trim.takeIf { it.isNotBlank() },
                                            color = color.takeIf { it.isNotBlank() },
                                            mileage = mileage.toIntOrNull(),
                                        ),
                                        auction = AdminAuctionInfoRequest(
                                            currentBid = lot.auction.currentBid,
                                            startingBid = startingBid.toDouble(),
                                            bidIncrement = bidIncrement.toDouble(),
                                            auctionType = auctionType,
                                            startTime = startTime,
                                            endTime = endTime,
                                            reservePrice = reservePrice.toDoubleOrNull(),
                                            buyItNowPrice = buyItNowPrice.toDoubleOrNull(),
                                        ),
                                        location = AdminLocationInfoRequest(
                                            address = address,
                                            city = city,
                                            state = state,
                                            zipCode = zipCode,
                                            country = country,
                                            timezone = timezone,
                                        ),
                                    )
                                )
                                onSaved()
                            } catch (e: Exception) {
                                error = "Error updating lot: ${e.message}"
                            } finally {
                                saving = false
                            }
                        }
                    }
                ) {
                    if (saving) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    else Text("Save")
                }
            }
        },
        dismissButton = {
            if (!isEditing) {
                OutlinedButton(onClick = onDismiss) { Text("Close") }
            } else {
                OutlinedButton(enabled = !saving, onClick = { isEditing = false; error = null }) { Text("Cancel") }
            }
        }
    )
}

@Composable
fun CreateLotDialog(onDismiss: () -> Unit, onCreated: () -> Unit) {
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Vehicle fields
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var vin by remember { mutableStateOf("") }
    var bodyType by remember { mutableStateOf("SEDAN") }
    var transmission by remember { mutableStateOf("AUTOMATIC") }
    var drivetrain by remember { mutableStateOf("FWD") }
    var fuelType by remember { mutableStateOf("GASOLINE") }
    var condition by remember { mutableStateOf("USED") }
    var engineType by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }

    // Auction fields
    var startingBid by remember { mutableStateOf("") }
    var currentBid by remember { mutableStateOf("") }
    var bidIncrement by remember { mutableStateOf("100") }
    var auctionType by remember { mutableStateOf("AUCTION") }

    // Location fields
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var timezone by remember { mutableStateOf("UTC") }

    // Seller fields
    var sellerId by remember { mutableStateOf("") }
    var sellerName by remember { mutableStateOf("") }
    var sellerType by remember { mutableStateOf("DEALER") }

    fun validate(): String? {
        if (make.isBlank()) return "Make is required"
        if (model.isBlank()) return "Model is required"
        if (year.toIntOrNull() == null) return "Valid year is required"
        if (engineType.isBlank()) return "Engine type is required"
        if (startingBid.toDoubleOrNull() == null) return "Valid starting bid is required"
        if (currentBid.toDoubleOrNull() == null) return "Valid current bid is required"
        if (bidIncrement.toDoubleOrNull() == null) return "Valid bid increment is required"
        if (address.isBlank()) return "Address is required"
        if (city.isBlank()) return "City is required"
        if (state.isBlank()) return "State is required"
        if (zipCode.isBlank()) return "ZIP code is required"
        if (country.isBlank()) return "Country is required"
        if (sellerId.isBlank()) return "Seller ID is required"
        if (sellerName.isBlank()) return "Seller name is required"
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

                SectionHeader("Vehicle Details")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(make, { make = it }, label = { Text("Make *") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(model, { model = it }, label = { Text("Model *") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(year, { year = it }, label = { Text("Year *") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(vin, { vin = it }, label = { Text("VIN") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(engineType, { engineType = it }, label = { Text("Engine Type *") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(mileage, { mileage = it }, label = { Text("Mileage") }, modifier = Modifier.weight(1f))
                }
                EnumDropdown("Body Type", bodyType, listOf("SEDAN","SUV","TRUCK","COUPE","CONVERTIBLE","HATCHBACK","WAGON","VAN","MINIVAN","OTHER")) { bodyType = it }
                EnumDropdown("Transmission", transmission, listOf("AUTOMATIC","MANUAL","CVT","DCT")) { transmission = it }
                EnumDropdown("Drivetrain", drivetrain, listOf("FWD","RWD","AWD","4WD")) { drivetrain = it }
                EnumDropdown("Fuel Type", fuelType, listOf("GASOLINE","DIESEL","ELECTRIC","HYBRID","PLUG_IN_HYBRID")) { fuelType = it }
                EnumDropdown("Condition", condition, listOf("NEW","USED","CERTIFIED","SALVAGE")) { condition = it }

                SectionHeader("Auction Details")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(startingBid, { startingBid = it }, label = { Text("Starting Bid *") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(currentBid, { currentBid = it }, label = { Text("Current Bid *") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(bidIncrement, { bidIncrement = it }, label = { Text("Bid Increment *") }, modifier = Modifier.weight(1f))
                    EnumDropdown("Auction Type", auctionType, listOf("AUCTION","BUY_IT_NOW","HYBRID"), modifier = Modifier.weight(1f)) { auctionType = it }
                }

                SectionHeader("Location")
                OutlinedTextField(address, { address = it }, label = { Text("Address *") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(city, { city = it }, label = { Text("City *") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(state, { state = it }, label = { Text("State *") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(zipCode, { zipCode = it }, label = { Text("ZIP *") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(country, { country = it }, label = { Text("Country *") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(timezone, { timezone = it }, label = { Text("Timezone *") }, modifier = Modifier.fillMaxWidth())

                SectionHeader("Seller")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(sellerId, { sellerId = it }, label = { Text("Seller ID *") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(sellerName, { sellerName = it }, label = { Text("Seller Name *") }, modifier = Modifier.weight(1f))
                }
                EnumDropdown("Seller Type", sellerType, listOf("DEALER","PRIVATE","AUCTION_HOUSE")) { sellerType = it }
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
                            createLot(AdminCreateLotRequest(
                                externalId = autoTitle.replace(" ", "_").lowercase(),
                                title = autoTitle,
                                description = "",
                                status = "DRAFT",
                                vehicle = AdminVehicleInfoRequest(
                                    make = make, model = model,
                                    year = year.toInt(),
                                    bodyType = bodyType,
                                    transmission = transmission,
                                    drivetrain = drivetrain,
                                    fuelType = fuelType,
                                    condition = condition,
                                    engine = AdminEngineInfoRequest(type = engineType),
                                    vin = vin.takeIf { it.isNotBlank() },
                                    mileage = mileage.toIntOrNull(),
                                ),
                                auction = AdminAuctionInfoRequest(
                                    currentBid = currentBid.toDouble(),
                                    startingBid = startingBid.toDouble(),
                                    bidIncrement = bidIncrement.toDouble(),
                                    auctionType = auctionType,
                                    startTime = "2025-01-01T00:00:00",
                                    endTime = "2025-12-31T00:00:00",
                                ),
                                location = AdminLocationInfoRequest(
                                    address = address, city = city, state = state,
                                    zipCode = zipCode, country = country, timezone = timezone,
                                ),
                                metadata = AdminLotMetadataRequest(
                                    sellerInfo = AdminSellerInfoRequest(
                                        id = sellerId, name = sellerName, type = sellerType
                                    )
                                )
                            ))
                            onCreated()
                        } catch (e: Exception) {
                            error = "Error creating lot: ${e.message}"
                        } finally {
                            saving = false
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
