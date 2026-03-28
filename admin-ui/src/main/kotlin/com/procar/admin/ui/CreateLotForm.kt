package com.procar.admin.ui

import com.procar.provider.admin.AdminAuctionInfoRequest
import com.procar.provider.admin.AdminCreateLotRequest
import com.procar.provider.admin.AdminEngineInfoRequest
import com.procar.provider.admin.AdminLocationInfoRequest
import com.procar.provider.admin.AdminLotMetadataRequest
import com.procar.provider.admin.AdminSellerInfoRequest
import com.procar.provider.admin.AdminVehicleInfoRequest
import com.procar.provider.admin.AdminWarehouseResponse
import com.procar.provider.lot.AuctionType
import com.procar.provider.lot.BodyType
import com.procar.provider.lot.DrivetrainType
import com.procar.provider.lot.FuelType
import com.procar.provider.lot.LotStatus
import com.procar.provider.lot.SellerType
import com.procar.provider.lot.TransmissionType
import com.procar.provider.lot.VehicleCondition
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.datetimepicker.DateTimePicker
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import java.time.LocalDateTime

enum class LocationMode { MANUAL, WAREHOUSE }

class CreateLotForm : VerticalLayout() {

    // Basic info
    val externalIdField = TextField("External ID")
    val titleField = TextField("Title")
    val descriptionField = TextArea("Description")
    val statusComboBox = ComboBox<LotStatus>("Status")

    // Vehicle
    val vinField = TextField("VIN *")
    val makeField = TextField("Make *")
    val modelField = TextField("Model *")
    val yearField = NumberField("Year *")
    val trimField = TextField("Trim")
    val bodyTypeField = ComboBox<BodyType>("Body Type *")
    val colorField = TextField("Color")
    val interiorColorField = TextField("Interior Color")
    val mileageField = NumberField("Mileage")
    val engineTypeField = TextField("Engine Type *")
    val engineDisplacementField = NumberField("Engine Displacement (L)")
    val engineCylindersField = NumberField("Engine Cylinders")
    val engineHorsepowerField = NumberField("Horsepower")
    val engineTorqueField = NumberField("Torque")
    val transmissionComboBox = ComboBox<TransmissionType>("Transmission *")
    val drivetrainComboBox = ComboBox<DrivetrainType>("Drivetrain *")
    val fuelTypeComboBox = ComboBox<FuelType>("Fuel Type *")
    val conditionComboBox = ComboBox<VehicleCondition>("Condition *")

    // Auction
    val currentBidField = NumberField("Current Bid *")
    val startingBidField = NumberField("Starting Bid *")
    val reservePriceField = NumberField("Reserve Price")
    val bidIncrementField = NumberField("Bid Increment *")
    val startTimePicker = DateTimePicker("Start Time *")
    val endTimePicker = DateTimePicker("End Time *")
    val auctionTypeComboBox = ComboBox<AuctionType>("Auction Type *")
    val buyItNowPriceField = NumberField("Buy It Now Price")

    // Location - manual fields
    val addressField = TextField("Address *")
    val cityField = TextField("City *")
    val stateField = TextField("State *")
    val zipCodeField = TextField("ZIP Code *")
    val countryField = TextField("Country *")
    val timezoneField = TextField("Timezone *")

    // Location - warehouse selection
    val locationModeGroup = RadioButtonGroup<LocationMode>("Location Type")
    val warehouseComboBox = ComboBox<AdminWarehouseResponse>("Warehouse *")

    // Seller / Metadata
    val sellerIdField = TextField("Seller ID *")
    val sellerNameField = TextField("Seller Name *")
    val sellerTypeComboBox = ComboBox<SellerType>("Seller Type *")
    val sellerRatingField = NumberField("Seller Rating")
    val sellerTotalSalesField = NumberField("Total Sales")
    val sellerResponseRateField = NumberField("Response Rate")

    init {
        setPadding(false)
        setSpacing(false)
        configureComboBoxes()
        applyDefaults()
        add(
            buildVehicleSection(),
            buildBasicInfoSection(),
            buildAuctionSection(),
            buildLocationMetadataSection()
        )
    }

    fun setWarehouses(warehouses: List<AdminWarehouseResponse>) {
        warehouseComboBox.setItems(warehouses)
    }

    private fun configureComboBoxes() {
        statusComboBox.setItems(LotStatus.entries)
        statusComboBox.isClearButtonVisible = false

        bodyTypeField.setItems(BodyType.entries)
        bodyTypeField.isClearButtonVisible = false

        transmissionComboBox.setItems(TransmissionType.entries)
        transmissionComboBox.isClearButtonVisible = false

        drivetrainComboBox.setItems(DrivetrainType.entries)
        drivetrainComboBox.isClearButtonVisible = false

        fuelTypeComboBox.setItems(FuelType.entries)
        fuelTypeComboBox.isClearButtonVisible = false

        conditionComboBox.setItems(VehicleCondition.entries)
        conditionComboBox.isClearButtonVisible = false

        auctionTypeComboBox.setItems(AuctionType.entries)
        auctionTypeComboBox.isClearButtonVisible = false

        sellerTypeComboBox.setItems(SellerType.entries)
        sellerTypeComboBox.isClearButtonVisible = false

        locationModeGroup.setItems(LocationMode.entries)
        locationModeGroup.value = LocationMode.MANUAL
        locationModeGroup.setItemLabelGenerator { if (it == LocationMode.MANUAL) "Enter manually" else "Select warehouse" }

        warehouseComboBox.setItemLabelGenerator { "${it.name} — ${it.city}, ${it.country}" }
        warehouseComboBox.isVisible = false
        warehouseComboBox.isClearButtonVisible = true

        locationModeGroup.addValueChangeListener { event ->
            val isWarehouse = event.value == LocationMode.WAREHOUSE
            warehouseComboBox.isVisible = isWarehouse
            addressField.isVisible = !isWarehouse
            cityField.isVisible = !isWarehouse
            stateField.isVisible = !isWarehouse
            zipCodeField.isVisible = !isWarehouse
            countryField.isVisible = !isWarehouse
            timezoneField.isVisible = !isWarehouse
        }
    }

    private fun applyDefaults() {
        startTimePicker.value = LocalDateTime.now().plusDays(1)
        endTimePicker.value = LocalDateTime.now().plusDays(7)
        timezoneField.value = "UTC"
    }

    private fun buildSection(title: String, vararg components: Component): Component {
        val section = VerticalLayout()
        section.addClassName("form-section")
        val heading = H2(title)
        heading.style.set("color", "var(--lumo-primary-color)")
        val form = FormLayout(*components)
        form.width = "100%"
        section.add(heading, form)
        return section
    }

    private fun buildBasicInfoSection(): Component = buildSection(
        "Basic Information",
        externalIdField, titleField, descriptionField, statusComboBox
    )

    private fun buildVehicleSection(): Component = buildSection(
        "Vehicle Details",
        vinField, makeField, modelField, yearField, trimField,
        bodyTypeField, colorField, interiorColorField, mileageField,
        engineTypeField, engineDisplacementField, engineCylindersField,
        engineHorsepowerField, engineTorqueField,
        transmissionComboBox, drivetrainComboBox, fuelTypeComboBox, conditionComboBox
    )

    private fun buildAuctionSection(): Component = buildSection(
        "Auction Details",
        currentBidField, startingBidField, reservePriceField, bidIncrementField,
        startTimePicker, endTimePicker, auctionTypeComboBox, buyItNowPriceField
    )

    private fun buildLocationMetadataSection(): Component {
        val section = VerticalLayout()
        section.addClassName("form-section")

        val heading = H2("Location & Seller Information")
        heading.style.set("color", "var(--lumo-primary-color)")
        section.add(heading)

        val twoColumns = HorizontalLayout()
        twoColumns.width = "100%"

        val locationColumn = VerticalLayout()
        locationColumn.addClassName("form-column")
        val locationTitle = H3("Location")
        locationTitle.style.set("color", "var(--lumo-secondary-color)")
        val manualLocationForm = FormLayout(addressField, cityField, stateField, zipCodeField, countryField, timezoneField)
        manualLocationForm.width = "100%"
        locationColumn.add(locationTitle, locationModeGroup, warehouseComboBox, manualLocationForm)

        val sellerColumn = VerticalLayout()
        sellerColumn.addClassName("form-column")
        val sellerTitle = H3("Seller Information")
        sellerTitle.style.set("color", "var(--lumo-secondary-color)")
        val sellerForm = FormLayout(
            sellerIdField, sellerNameField, sellerTypeComboBox,
            sellerRatingField, sellerTotalSalesField, sellerResponseRateField
        )
        sellerForm.width = "100%"
        sellerColumn.add(sellerTitle, sellerForm)

        twoColumns.add(locationColumn, sellerColumn)
        section.add(twoColumns)
        return section
    }

    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        if (makeField.value.isNullOrBlank()) errors.add("Make is required")
        if (modelField.value.isNullOrBlank()) errors.add("Model is required")
        if (yearField.value == null) errors.add("Year is required")
        if (bodyTypeField.value == null) errors.add("Body Type is required")
        if (engineTypeField.value.isNullOrBlank()) errors.add("Engine Type is required")
        if (transmissionComboBox.value == null) errors.add("Transmission is required")
        if (drivetrainComboBox.value == null) errors.add("Drivetrain is required")
        if (fuelTypeComboBox.value == null) errors.add("Fuel Type is required")
        if (conditionComboBox.value == null) errors.add("Condition is required")

        if (currentBidField.value == null) errors.add("Current Bid is required")
        if (startingBidField.value == null) errors.add("Starting Bid is required")
        if (bidIncrementField.value == null) errors.add("Bid Increment is required")
        if (startTimePicker.value == null) errors.add("Start Time is required")
        if (endTimePicker.value == null) errors.add("End Time is required")
        if (auctionTypeComboBox.value == null) errors.add("Auction Type is required")

        if (locationModeGroup.value == LocationMode.WAREHOUSE) {
            if (warehouseComboBox.value == null) errors.add("Warehouse is required")
        } else {
            if (addressField.value.isNullOrBlank()) errors.add("Address is required")
            if (cityField.value.isNullOrBlank()) errors.add("City is required")
            if (stateField.value.isNullOrBlank()) errors.add("State is required")
            if (zipCodeField.value.isNullOrBlank()) errors.add("ZIP Code is required")
            if (countryField.value.isNullOrBlank()) errors.add("Country is required")
            if (timezoneField.value.isNullOrBlank()) errors.add("Timezone is required")
        }

        if (sellerIdField.value.isNullOrBlank()) errors.add("Seller ID is required")
        if (sellerNameField.value.isNullOrBlank()) errors.add("Seller Name is required")
        if (sellerTypeComboBox.value == null) errors.add("Seller Type is required")

        if (startTimePicker.value != null && endTimePicker.value != null &&
            startTimePicker.value!!.isAfter(endTimePicker.value!!)
        ) errors.add("Start time must be before end time")

        if (startingBidField.value != null && startingBidField.value!! <= 0)
            errors.add("Starting bid must be greater than 0")

        if (bidIncrementField.value != null && bidIncrementField.value!! <= 0)
            errors.add("Bid increment must be greater than 0")

        if (reservePriceField.value != null && startingBidField.value != null &&
            reservePriceField.value!! < startingBidField.value!!
        ) errors.add("Reserve price cannot be less than starting bid")

        return errors
    }

    fun buildRequest(): AdminCreateLotRequest {
        val autoTitle = buildAutoTitle()
        val finalTitle = titleField.value.takeUnless { it.isNullOrBlank() } ?: autoTitle

        return AdminCreateLotRequest(
            externalId = externalIdField.value ?: finalTitle.replace("\\s+".toRegex(), "_").lowercase(),
            title = finalTitle,
            description = descriptionField.value ?: "",
            vehicle = AdminVehicleInfoRequest(
                vin = vinField.value,
                make = makeField.value,
                model = modelField.value,
                year = yearField.value!!.toInt(),
                trim = trimField.value ?: "",
                bodyType = bodyTypeField.value,
                color = colorField.value ?: "",
                interiorColor = interiorColorField.value ?: "",
                mileage = mileageField.value?.toInt(),
                engine = AdminEngineInfoRequest(
                    type = engineTypeField.value,
                    displacement = engineDisplacementField.value,
                    cylinders = engineCylindersField.value?.toInt(),
                    horsepower = engineHorsepowerField.value?.toInt(),
                    torque = engineTorqueField.value?.toInt()
                ),
                transmission = transmissionComboBox.value!!,
                drivetrain = drivetrainComboBox.value!!,
                fuelType = fuelTypeComboBox.value!!,
                condition = conditionComboBox.value!!
            ),
            auction = AdminAuctionInfoRequest(
                currentBid = currentBidField.value!!,
                startingBid = startingBidField.value!!,
                reservePrice = reservePriceField.value,
                bidIncrement = bidIncrementField.value!!,
                startTime = startTimePicker.value!!,
                endTime = endTimePicker.value!!,
                auctionType = auctionTypeComboBox.value!!,
                buyItNowPrice = buyItNowPriceField.value
            ),
            location = if (locationModeGroup.value == LocationMode.WAREHOUSE) {
                val wh = warehouseComboBox.value!!
                AdminLocationInfoRequest(
                    address = wh.address,
                    city = wh.city,
                    state = wh.state,
                    zipCode = wh.zipCode,
                    country = wh.country,
                    coordinates = wh.coordinates,
                    timezone = wh.timezone
                )
            } else {
                AdminLocationInfoRequest(
                    address = addressField.value,
                    city = cityField.value,
                    state = stateField.value,
                    zipCode = zipCodeField.value,
                    country = countryField.value,
                    timezone = timezoneField.value
                )
            },
            metadata = AdminLotMetadataRequest(
                sellerInfo = AdminSellerInfoRequest(
                    id = sellerIdField.value,
                    name = sellerNameField.value,
                    type = sellerTypeComboBox.value!!,
                    rating = sellerRatingField.value,
                    totalSales = sellerTotalSalesField.value?.toInt(),
                    responseRate = sellerResponseRateField.value
                )
            ),
            status = statusComboBox.value ?: LotStatus.DRAFT
        )
    }

    private fun buildAutoTitle(): String {
        val year = yearField.value?.toInt() ?: return ""
        val make = makeField.value ?: return ""
        val model = modelField.value ?: return ""

        val parts = mutableListOf(year.toString(), make, model)
        engineDisplacementField.value?.takeIf { it > 0 }?.let { parts.add(String.format("%.1f", it)) }
        engineHorsepowerField.value?.toInt()?.let { parts.add("${it}HP") }
        return parts.joinToString(" ")
    }
}
