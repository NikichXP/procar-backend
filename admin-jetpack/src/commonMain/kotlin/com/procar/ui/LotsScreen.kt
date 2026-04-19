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
            onSaved = { loadLots() }
        )
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
private fun SectionActions(dirty: Boolean, saving: Boolean, onRevert: () -> Unit, onSave: () -> Unit) {
    if (!dirty) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(enabled = !saving, onClick = onRevert) { Text("Revert") }
        Button(enabled = !saving, onClick = onSave) {
            if (saving) CircularProgressIndicator(modifier = Modifier.size(16.dp))
            else Text("Save")
        }
    }
}

@Composable
fun LotDetailsDialog(lot: AdminLotResponse, onDismiss: () -> Unit, onSaved: () -> Unit) {
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    var savingSection by remember { mutableStateOf<String?>(null) }

    // --- General section ---
    var origTitle by remember(lot.id) { mutableStateOf(lot.title) }
    var origDescription by remember(lot.id) { mutableStateOf(lot.description) }
    var origStatus by remember(lot.id) { mutableStateOf(lot.status) }
    var title by remember(lot.id) { mutableStateOf(lot.title) }
    var description by remember(lot.id) { mutableStateOf(lot.description) }
    var status by remember(lot.id) { mutableStateOf(lot.status) }
    val titleDirty = title != origTitle
    val descriptionDirty = description != origDescription
    val statusDirty = status != origStatus
    val generalDirty = titleDirty || descriptionDirty || statusDirty

    // --- Vehicle section ---
    var origMake by remember(lot.id) { mutableStateOf(lot.vehicle.make) }
    var origModel by remember(lot.id) { mutableStateOf(lot.vehicle.model) }
    var origYear by remember(lot.id) { mutableStateOf(lot.vehicle.year.toString()) }
    var origVin by remember(lot.id) { mutableStateOf(lot.vehicle.vin ?: "") }
    var origTrim by remember(lot.id) { mutableStateOf(lot.vehicle.trim ?: "") }
    var origColor by remember(lot.id) { mutableStateOf(lot.vehicle.color ?: "") }
    var origMileage by remember(lot.id) { mutableStateOf(lot.vehicle.mileage?.toString() ?: "") }
    var origBodyType by remember(lot.id) { mutableStateOf(lot.vehicle.bodyType) }
    var origTransmission by remember(lot.id) { mutableStateOf(lot.vehicle.transmission) }
    var origDrivetrain by remember(lot.id) { mutableStateOf(lot.vehicle.drivetrain ?: "FWD") }
    var origFuelType by remember(lot.id) { mutableStateOf(lot.vehicle.fuelType) }
    var origCondition by remember(lot.id) { mutableStateOf(lot.vehicle.condition) }
    var origEngineType by remember(lot.id) { mutableStateOf(lot.vehicle.engine?.type ?: "") }

    var make by remember(lot.id) { mutableStateOf(origMake) }
    var model by remember(lot.id) { mutableStateOf(origModel) }
    var year by remember(lot.id) { mutableStateOf(origYear) }
    var vin by remember(lot.id) { mutableStateOf(origVin) }
    var trim by remember(lot.id) { mutableStateOf(origTrim) }
    var color by remember(lot.id) { mutableStateOf(origColor) }
    var mileage by remember(lot.id) { mutableStateOf(origMileage) }
    var bodyType by remember(lot.id) { mutableStateOf(origBodyType) }
    var transmission by remember(lot.id) { mutableStateOf(origTransmission) }
    var drivetrain by remember(lot.id) { mutableStateOf(origDrivetrain) }
    var fuelType by remember(lot.id) { mutableStateOf(origFuelType) }
    var condition by remember(lot.id) { mutableStateOf(origCondition) }
    var engineType by remember(lot.id) { mutableStateOf(origEngineType) }

    val vehicleDirty = make != origMake || model != origModel || year != origYear ||
        vin != origVin || trim != origTrim || color != origColor || mileage != origMileage ||
        bodyType != origBodyType || transmission != origTransmission || drivetrain != origDrivetrain ||
        fuelType != origFuelType || condition != origCondition || engineType != origEngineType

    // --- Auction section (currentBid not editable) ---
    var origStartingBid by remember(lot.id) { mutableStateOf(lot.auction.startingBid.toString()) }
    var origBidIncrement by remember(lot.id) { mutableStateOf(lot.auction.bidIncrement.toString()) }
    var origAuctionType by remember(lot.id) { mutableStateOf(lot.auction.auctionType) }
    var origReservePrice by remember(lot.id) { mutableStateOf(lot.auction.reservePrice?.toString() ?: "") }
    var origBuyItNowPrice by remember(lot.id) { mutableStateOf(lot.auction.buyItNowPrice?.toString() ?: "") }
    var origStartTime by remember(lot.id) { mutableStateOf(lot.auction.startTime) }
    var origEndTime by remember(lot.id) { mutableStateOf(lot.auction.endTime) }

    var startingBid by remember(lot.id) { mutableStateOf(origStartingBid) }
    var bidIncrement by remember(lot.id) { mutableStateOf(origBidIncrement) }
    var auctionType by remember(lot.id) { mutableStateOf(origAuctionType) }
    var reservePrice by remember(lot.id) { mutableStateOf(origReservePrice) }
    var buyItNowPrice by remember(lot.id) { mutableStateOf(origBuyItNowPrice) }
    var startTime by remember(lot.id) { mutableStateOf(origStartTime) }
    var endTime by remember(lot.id) { mutableStateOf(origEndTime) }

    val auctionDirty = startingBid != origStartingBid || bidIncrement != origBidIncrement ||
        auctionType != origAuctionType || reservePrice != origReservePrice ||
        buyItNowPrice != origBuyItNowPrice || startTime != origStartTime || endTime != origEndTime

    // --- Location section ---
    var origAddress by remember(lot.id) { mutableStateOf(lot.location.address) }
    var origCity by remember(lot.id) { mutableStateOf(lot.location.city) }
    var origState by remember(lot.id) { mutableStateOf(lot.location.state) }
    var origZipCode by remember(lot.id) { mutableStateOf(lot.location.zipCode) }
    var origCountry by remember(lot.id) { mutableStateOf(lot.location.country) }
    var origTimezone by remember(lot.id) { mutableStateOf(lot.location.timezone) }

    var address by remember(lot.id) { mutableStateOf(origAddress) }
    var city by remember(lot.id) { mutableStateOf(origCity) }
    var state by remember(lot.id) { mutableStateOf(origState) }
    var zipCode by remember(lot.id) { mutableStateOf(origZipCode) }
    var country by remember(lot.id) { mutableStateOf(origCountry) }
    var timezone by remember(lot.id) { mutableStateOf(origTimezone) }

    val locationDirty = address != origAddress || city != origCity || state != origState ||
        zipCode != origZipCode || country != origCountry || timezone != origTimezone

    val anyDirty = generalDirty || vehicleDirty || auctionDirty || locationDirty

    fun saveSection(sectionName: String, build: () -> AdminUpdateLotRequest, onSuccess: () -> Unit) {
        scope.launch {
            savingSection = sectionName
            error = null
            try {
                updateLot(lot.id, build())
                onSuccess()
                onSaved()
            } catch (e: Exception) {
                error = "Error updating $sectionName: ${e.message}"
            } finally {
                savingSection = null
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!anyDirty && savingSection == null) onDismiss() },
        title = { Text(lot.title.ifBlank { "Lot Details" }) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).width(520.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                // General
                SectionHeader("General")
                DetailRow("ID", lot.id)
                DetailRow("External ID", lot.externalId)
                DetailRow("Created", lot.createdAt)
                DetailRow("Updated", lot.updatedAt)
                OutlinedTextField(
                    title, { title = it },
                    label = { Text("Title *") },
                    colors = modifiedFieldColors(titleDirty),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    description, { description = it },
                    label = { Text("Description") },
                    colors = modifiedFieldColors(descriptionDirty),
                    modifier = Modifier.fillMaxWidth(),
                )
                EnumDropdown(
                    "Status", status,
                    listOf("DRAFT","PENDING","ACTIVE","SOLD","CANCELLED","EXPIRED","HIDDEN"),
                    modified = statusDirty,
                ) { status = it }
                SectionActions(
                    dirty = generalDirty,
                    saving = savingSection == "General",
                    onRevert = { title = origTitle; description = origDescription; status = origStatus },
                    onSave = {
                        if (title.isBlank()) { error = "Title is required"; return@SectionActions }
                        saveSection("General", {
                            AdminUpdateLotRequest(
                                title = if (titleDirty) title else null,
                                description = if (descriptionDirty) description else null,
                                status = if (statusDirty) status else null,
                            )
                        }) {
                            origTitle = title
                            origDescription = description
                            origStatus = status
                        }
                    }
                )

                // Vehicle
                SectionHeader("Vehicle")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(make, { make = it }, label = { Text("Make *") },
                        colors = modifiedFieldColors(make != origMake), modifier = Modifier.weight(1f))
                    OutlinedTextField(model, { model = it }, label = { Text("Model *") },
                        colors = modifiedFieldColors(model != origModel), modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(year, { year = it }, label = { Text("Year *") },
                        colors = modifiedFieldColors(year != origYear), modifier = Modifier.weight(1f))
                    OutlinedTextField(vin, { vin = it }, label = { Text("VIN") },
                        colors = modifiedFieldColors(vin != origVin), modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(trim, { trim = it }, label = { Text("Trim") },
                        colors = modifiedFieldColors(trim != origTrim), modifier = Modifier.weight(1f))
                    OutlinedTextField(color, { color = it }, label = { Text("Color") },
                        colors = modifiedFieldColors(color != origColor), modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(engineType, { engineType = it }, label = { Text("Engine Type *") },
                        colors = modifiedFieldColors(engineType != origEngineType), modifier = Modifier.weight(1f))
                    OutlinedTextField(mileage, { mileage = it }, label = { Text("Mileage") },
                        colors = modifiedFieldColors(mileage != origMileage), modifier = Modifier.weight(1f))
                }
                EnumDropdown("Body Type", bodyType,
                    listOf("SEDAN","SUV","TRUCK","COUPE","CONVERTIBLE","HATCHBACK","WAGON","VAN","MINIVAN","OTHER"),
                    modified = bodyType != origBodyType) { bodyType = it }
                EnumDropdown("Transmission", transmission,
                    listOf("AUTOMATIC","MANUAL","CVT","DCT"),
                    modified = transmission != origTransmission) { transmission = it }
                EnumDropdown("Drivetrain", drivetrain,
                    listOf("FWD","RWD","AWD","4WD"),
                    modified = drivetrain != origDrivetrain) { drivetrain = it }
                EnumDropdown("Fuel Type", fuelType,
                    listOf("GASOLINE","DIESEL","ELECTRIC","HYBRID","PLUG_IN_HYBRID"),
                    modified = fuelType != origFuelType) { fuelType = it }
                EnumDropdown("Condition", condition,
                    listOf("NEW","USED","CERTIFIED","SALVAGE"),
                    modified = condition != origCondition) { condition = it }
                SectionActions(
                    dirty = vehicleDirty,
                    saving = savingSection == "Vehicle",
                    onRevert = {
                        make = origMake; model = origModel; year = origYear; vin = origVin
                        trim = origTrim; color = origColor; mileage = origMileage
                        bodyType = origBodyType; transmission = origTransmission
                        drivetrain = origDrivetrain; fuelType = origFuelType
                        condition = origCondition; engineType = origEngineType
                    },
                    onSave = {
                        if (make.isBlank()) { error = "Make is required"; return@SectionActions }
                        if (model.isBlank()) { error = "Model is required"; return@SectionActions }
                        if (year.toIntOrNull() == null) { error = "Valid year is required"; return@SectionActions }
                        if (engineType.isBlank()) { error = "Engine type is required"; return@SectionActions }
                        saveSection("Vehicle", {
                            AdminUpdateLotRequest(
                                vehicle = AdminVehicleInfoRequest(
                                    make = make, model = model, year = year.toInt(),
                                    bodyType = bodyType, transmission = transmission,
                                    drivetrain = drivetrain, fuelType = fuelType,
                                    condition = condition,
                                    engine = AdminEngineInfoRequest(type = engineType),
                                    vin = vin.takeIf { it.isNotBlank() },
                                    trim = trim.takeIf { it.isNotBlank() },
                                    color = color.takeIf { it.isNotBlank() },
                                    mileage = mileage.toIntOrNull(),
                                )
                            )
                        }) {
                            origMake = make; origModel = model; origYear = year; origVin = vin
                            origTrim = trim; origColor = color; origMileage = mileage
                            origBodyType = bodyType; origTransmission = transmission
                            origDrivetrain = drivetrain; origFuelType = fuelType
                            origCondition = condition; origEngineType = engineType
                        }
                    }
                )

                // Auction
                SectionHeader("Auction")
                DetailRow("Current Bid", "$${lot.auction.currentBid}")
                DetailRow("Total Bids", lot.auction.totalBids.toString())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(startingBid, { startingBid = it }, label = { Text("Starting Bid *") },
                        colors = modifiedFieldColors(startingBid != origStartingBid), modifier = Modifier.weight(1f))
                    OutlinedTextField(bidIncrement, { bidIncrement = it }, label = { Text("Bid Increment *") },
                        colors = modifiedFieldColors(bidIncrement != origBidIncrement), modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(reservePrice, { reservePrice = it }, label = { Text("Reserve Price") },
                        colors = modifiedFieldColors(reservePrice != origReservePrice), modifier = Modifier.weight(1f))
                    OutlinedTextField(buyItNowPrice, { buyItNowPrice = it }, label = { Text("Buy It Now") },
                        colors = modifiedFieldColors(buyItNowPrice != origBuyItNowPrice), modifier = Modifier.weight(1f))
                }
                EnumDropdown("Auction Type", auctionType,
                    listOf("AUCTION","BUY_IT_NOW","HYBRID"),
                    modified = auctionType != origAuctionType) { auctionType = it }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(startTime, { startTime = it }, label = { Text("Start Time *") },
                        colors = modifiedFieldColors(startTime != origStartTime), modifier = Modifier.weight(1f))
                    OutlinedTextField(endTime, { endTime = it }, label = { Text("End Time *") },
                        colors = modifiedFieldColors(endTime != origEndTime), modifier = Modifier.weight(1f))
                }
                SectionActions(
                    dirty = auctionDirty,
                    saving = savingSection == "Auction",
                    onRevert = {
                        startingBid = origStartingBid; bidIncrement = origBidIncrement
                        auctionType = origAuctionType; reservePrice = origReservePrice
                        buyItNowPrice = origBuyItNowPrice
                        startTime = origStartTime; endTime = origEndTime
                    },
                    onSave = {
                        if (startingBid.toDoubleOrNull() == null) { error = "Valid starting bid is required"; return@SectionActions }
                        if (bidIncrement.toDoubleOrNull() == null) { error = "Valid bid increment is required"; return@SectionActions }
                        saveSection("Auction", {
                            AdminUpdateLotRequest(
                                auction = AdminAuctionInfoRequest(
                                    currentBid = lot.auction.currentBid,
                                    startingBid = startingBid.toDouble(),
                                    bidIncrement = bidIncrement.toDouble(),
                                    auctionType = auctionType,
                                    startTime = startTime,
                                    endTime = endTime,
                                    reservePrice = reservePrice.toDoubleOrNull(),
                                    buyItNowPrice = buyItNowPrice.toDoubleOrNull(),
                                )
                            )
                        }) {
                            origStartingBid = startingBid; origBidIncrement = bidIncrement
                            origAuctionType = auctionType; origReservePrice = reservePrice
                            origBuyItNowPrice = buyItNowPrice
                            origStartTime = startTime; origEndTime = endTime
                        }
                    }
                )

                // Location
                SectionHeader("Location")
                OutlinedTextField(address, { address = it }, label = { Text("Address *") },
                    colors = modifiedFieldColors(address != origAddress), modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(city, { city = it }, label = { Text("City *") },
                        colors = modifiedFieldColors(city != origCity), modifier = Modifier.weight(1f))
                    OutlinedTextField(state, { state = it }, label = { Text("State *") },
                        colors = modifiedFieldColors(state != origState), modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(zipCode, { zipCode = it }, label = { Text("ZIP *") },
                        colors = modifiedFieldColors(zipCode != origZipCode), modifier = Modifier.weight(1f))
                    OutlinedTextField(country, { country = it }, label = { Text("Country *") },
                        colors = modifiedFieldColors(country != origCountry), modifier = Modifier.weight(1f))
                }
                OutlinedTextField(timezone, { timezone = it }, label = { Text("Timezone *") },
                    colors = modifiedFieldColors(timezone != origTimezone), modifier = Modifier.fillMaxWidth())
                SectionActions(
                    dirty = locationDirty,
                    saving = savingSection == "Location",
                    onRevert = {
                        address = origAddress; city = origCity; state = origState
                        zipCode = origZipCode; country = origCountry; timezone = origTimezone
                    },
                    onSave = {
                        if (address.isBlank()) { error = "Address is required"; return@SectionActions }
                        if (city.isBlank()) { error = "City is required"; return@SectionActions }
                        if (state.isBlank()) { error = "State is required"; return@SectionActions }
                        if (zipCode.isBlank()) { error = "ZIP code is required"; return@SectionActions }
                        if (country.isBlank()) { error = "Country is required"; return@SectionActions }
                        if (timezone.isBlank()) { error = "Timezone is required"; return@SectionActions }
                        saveSection("Location", {
                            AdminUpdateLotRequest(
                                location = AdminLocationInfoRequest(
                                    address = address, city = city, state = state,
                                    zipCode = zipCode, country = country, timezone = timezone,
                                )
                            )
                        }) {
                            origAddress = address; origCity = city; origState = state
                            origZipCode = zipCode; origCountry = country; origTimezone = timezone
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                enabled = savingSection == null,
                onClick = onDismiss,
            ) { Text(if (anyDirty) "Close (discard changes)" else "Close") }
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
