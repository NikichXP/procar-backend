package com.procar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.procar.model.AdminLotResponse

private object VehicleOptions {
    val bodyType = listOf("SEDAN","SUV","TRUCK","COUPE","CONVERTIBLE","HATCHBACK","WAGON","VAN","MINIVAN","OTHER")
    val transmission = listOf("AUTOMATIC","MANUAL","CVT","DCT")
    val drivetrain = listOf("FWD","RWD","AWD","4WD")
    val fuelType = listOf("GASOLINE","DIESEL","ELECTRIC","HYBRID","PLUG_IN_HYBRID")
    val condition = listOf("NEW","USED","CERTIFIED","SALVAGE")
}

private val StatusOptions = listOf("DRAFT","PENDING","ACTIVE","SOLD","CANCELLED","EXPIRED","HIDDEN")
private val AuctionTypeOptions = listOf("AUCTION","BUY_IT_NOW","HYBRID")

@Composable
fun GeneralSection(
    lot: AdminLotResponse,
    state: GeneralEditState,
    saving: Boolean,
    onSave: () -> Unit,
) {
    SectionHeader("General")
    DetailRow("ID", lot.id)
    DetailRow("External ID", lot.externalId)
    DetailRow("Created", lot.createdAt)
    DetailRow("Updated", lot.updatedAt)
    EditableTextField(state.title, "Title *")
    EditableTextField(state.description, "Description")
    EditableEnumField(state.status, "Status", StatusOptions)
    SectionActions(
        modified = state.isModified,
        saving = saving,
        onRevert = { state.revert() },
        onSave = onSave,
    )
}

@Composable
fun VehicleSection(
    state: VehicleEditState,
    saving: Boolean,
    onSave: () -> Unit,
) {
    SectionHeader("Vehicle")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EditableTextField(state.make, "Make *", Modifier.weight(1f))
        EditableTextField(state.model, "Model *", Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EditableTextField(state.year, "Year *", Modifier.weight(1f))
        EditableTextField(state.vin, "VIN", Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EditableTextField(state.trim, "Trim", Modifier.weight(1f))
        EditableTextField(state.color, "Color", Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EditableTextField(state.engineType, "Engine Type *", Modifier.weight(1f))
        EditableTextField(state.mileage, "Mileage", Modifier.weight(1f))
    }
    EditableEnumField(state.bodyType, "Body Type", VehicleOptions.bodyType)
    EditableEnumField(state.transmission, "Transmission", VehicleOptions.transmission)
    EditableEnumField(state.drivetrain, "Drivetrain", VehicleOptions.drivetrain)
    EditableEnumField(state.fuelType, "Fuel Type", VehicleOptions.fuelType)
    EditableEnumField(state.condition, "Condition", VehicleOptions.condition)
    SectionActions(
        modified = state.isModified,
        saving = saving,
        onRevert = { state.revert() },
        onSave = onSave,
    )
}

@Composable
fun AuctionSection(
    lot: AdminLotResponse,
    state: AuctionEditState,
    saving: Boolean,
    onSave: () -> Unit,
) {
    SectionHeader("Auction")
    DetailRow("Current Bid", "$${lot.auction.currentBid}")
    DetailRow("Total Bids", lot.auction.totalBids.toString())
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EditableTextField(state.startingBid, "Starting Bid *", Modifier.weight(1f))
        EditableTextField(state.bidIncrement, "Bid Increment *", Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EditableTextField(state.reservePrice, "Reserve Price", Modifier.weight(1f))
        EditableTextField(state.buyItNowPrice, "Buy It Now", Modifier.weight(1f))
    }
    EditableEnumField(state.auctionType, "Auction Type", AuctionTypeOptions)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EditableTextField(state.startTime, "Start Time *", Modifier.weight(1f))
        EditableTextField(state.endTime, "End Time *", Modifier.weight(1f))
    }
    SectionActions(
        modified = state.isModified,
        saving = saving,
        onRevert = { state.revert() },
        onSave = onSave,
    )
}

@Composable
fun LocationSection(
    state: LocationEditState,
    saving: Boolean,
    onSave: () -> Unit,
) {
    SectionHeader("Location")
    EditableTextField(state.address, "Address *")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EditableTextField(state.city, "City *", Modifier.weight(1f))
        EditableTextField(state.state, "State *", Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EditableTextField(state.zipCode, "ZIP *", Modifier.weight(1f))
        EditableTextField(state.country, "Country *", Modifier.weight(1f))
    }
    EditableTextField(state.timezone, "Timezone *")
    SectionActions(
        modified = state.isModified,
        saving = saving,
        onRevert = { state.revert() },
        onSave = onSave,
    )
}
