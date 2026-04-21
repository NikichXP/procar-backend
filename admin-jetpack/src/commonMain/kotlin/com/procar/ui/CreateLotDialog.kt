package com.procar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.procar.api.createLot
import com.procar.model.*
import kotlinx.coroutines.launch

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
        validateLocationFields(address, city, state, zipCode, country)?.let { return it }
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
