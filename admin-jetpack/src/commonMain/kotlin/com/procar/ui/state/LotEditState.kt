package com.procar.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.procar.gateway.api.dto.*
import com.procar.ui.components.FieldState
import com.procar.ui.components.SectionEditState
import com.procar.ui.components.validateLocationFields

class GeneralEditState(lot: AdminLotResponse) : SectionEditState {
    val title = FieldState(lot.title)
    val description = FieldState(lot.description)
    val status = FieldState(lot.status.name)
    val lotType = FieldState(lot.lotType.name)
    val brand = FieldState(lot.brand.name)
    val buyoutPrice = FieldState(lot.buyoutPrice?.toString() ?: "")

    private val fields: List<FieldState<*>> = listOf(title, description, status, lotType, brand, buyoutPrice)

    override val isModified: Boolean get() = fields.any { it.isModified }
    val isOnlyStatusModified: Boolean get() = status.isModified && fields.filterNot { it === status }.none { it.isModified }
    override fun revert() = fields.forEach { it.revert() }
    override fun commit() = fields.forEach { it.commit() }

    override fun validate(): String? {
        if (title.current.isBlank()) return "Title is required"
        if (lotType.current != "AUCTION" && buyoutPrice.current.toDoubleOrNull() == null) {
            return "Valid buyout price is required"
        }
        return null
    }

    override fun buildRequest(): AdminUpdateLotRequest = AdminUpdateLotRequest(
        title = if (title.isModified) title.current else null,
        description = if (description.isModified) description.current else null,
        status = if (status.isModified) AdminLotStatus.valueOf(status.current) else null,
        lotType = if (lotType.isModified) LotType.valueOf(lotType.current) else null,
        brand = if (brand.isModified) LotBrand.valueOf(brand.current) else null,
        buyoutPrice = if (buyoutPrice.isModified) buyoutPrice.current.toDoubleOrNull() else null,
    )
}

class VehicleEditState(v: AdminVehicleInfoResponse) : SectionEditState {
    val make = FieldState(v.make)
    val model = FieldState(v.model)
    val year = FieldState(v.year.toString())
    val vin = FieldState(v.vin ?: "")
    val trim = FieldState(v.trim ?: "")
    val color = FieldState(v.color ?: "")
    val mileage = FieldState(v.mileage?.toString() ?: "")
    val bodyType = FieldState(v.bodyType)
    val transmission = FieldState(v.transmission)
    val drivetrain = FieldState(v.drivetrain ?: "FWD")
    val fuelType = FieldState(v.fuelType)
    val condition = FieldState(v.condition)
    val engineType = FieldState(v.engine?.type ?: "")

    private val fields: List<FieldState<*>> = listOf(
        make, model, year, vin, trim, color, mileage,
        bodyType, transmission, drivetrain, fuelType, condition, engineType,
    )

    override val isModified: Boolean get() = fields.any { it.isModified }
    override fun revert() = fields.forEach { it.revert() }
    override fun commit() = fields.forEach { it.commit() }

    override fun validate(): String? {
        if (make.current.isBlank()) return "Make is required"
        if (model.current.isBlank()) return "Model is required"
        if (year.current.toIntOrNull() == null) return "Valid year is required"
        if (engineType.current.isBlank()) return "Cylinder count/shape is required"
        return null
    }

    override fun buildRequest(): AdminUpdateLotRequest = AdminUpdateLotRequest(
        vehicle = AdminVehicleInfoRequest(
            make = make.current,
            model = model.current,
            year = year.current.toInt(),
            bodyType = bodyType.current,
            transmission = transmission.current,
            drivetrain = drivetrain.current,
            fuelType = fuelType.current,
            condition = condition.current,
            engine = AdminEngineInfoRequest(type = engineType.current),
            vin = vin.current.takeIf { it.isNotBlank() },
            trim = trim.current.takeIf { it.isNotBlank() },
            color = color.current.takeIf { it.isNotBlank() },
            mileage = mileage.current.toIntOrNull(),
        )
    )
}

class AuctionEditState(private val source: AdminAuctionInfoResponse) : SectionEditState {
    val startingBid = FieldState(source.startingBid.toString())
    val bidIncrement = FieldState(source.bidIncrement.toString())
    val reservePrice = FieldState(source.reservePrice?.toString() ?: "")
    val startTime = FieldState(source.startTime)
    val endTime = FieldState(source.endTime)

    private val fields: List<FieldState<*>> = listOf(
        startingBid, bidIncrement, reservePrice, startTime, endTime,
    )

    override val isModified: Boolean get() = fields.any { it.isModified }
    override fun revert() = fields.forEach { it.revert() }
    override fun commit() = fields.forEach { it.commit() }

    override fun validate(): String? {
        if (startingBid.current.toDoubleOrNull() == null) return "Valid starting bid is required"
        if (bidIncrement.current.toDoubleOrNull() == null) return "Valid bid increment is required"
        return null
    }

    override fun buildRequest(): AdminUpdateLotRequest = AdminUpdateLotRequest(
        auction = AdminAuctionInfoRequest(
            currentBid = source.currentBid,
            startingBid = startingBid.current.toDouble(),
            bidIncrement = bidIncrement.current.toDouble(),
            startTime = startTime.current,
            endTime = endTime.current,
            reservePrice = reservePrice.current.toDoubleOrNull(),
        )
    )
}

class LocationEditState(l: AdminLocationInfoResponse) : SectionEditState {
    val address = FieldState(l.address)
    val city = FieldState(l.city)
    val state = FieldState(l.state)
    val zipCode = FieldState(l.zipCode)
    val country = FieldState(l.country)
    val timezone = FieldState(l.timezone)

    private val fields: List<FieldState<*>> = listOf(address, city, state, zipCode, country, timezone)

    override val isModified: Boolean get() = fields.any { it.isModified }
    override fun revert() = fields.forEach { it.revert() }
    override fun commit() = fields.forEach { it.commit() }

    override fun validate(): String? = validateLocationFields(
        address.current, city.current, state.current, zipCode.current, country.current, timezone.current,
    )

    override fun buildRequest(): AdminUpdateLotRequest = AdminUpdateLotRequest(
        location = AdminLocationInfoRequest(
            address = address.current,
            city = city.current,
            state = state.current,
            zipCode = zipCode.current,
            country = country.current,
            timezone = timezone.current,
        )
    )
}

class LotEditState(lot: AdminLotResponse) {
    val general = GeneralEditState(lot)
    val vehicle = VehicleEditState(lot.vehicle)
    val auction = lot.auction?.let { AuctionEditState(it) }
    val location = LocationEditState(lot.location)

    val isAnyModified: Boolean
        get() = general.isModified || vehicle.isModified || (auction?.isModified == true) || location.isModified
}

@Composable
fun rememberLotEditState(lot: AdminLotResponse): LotEditState =
    remember(lot.id) { LotEditState(lot) }
