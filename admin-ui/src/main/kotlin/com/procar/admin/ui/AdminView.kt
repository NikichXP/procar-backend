package com.procar.admin.ui

import com.procar.admin.service.GatewayClientService
import com.procar.provider.admin.AdminAuctionInfoRequest
import com.procar.provider.admin.AdminCreateLotRequest
import com.procar.provider.admin.AdminEngineInfoRequest
import com.procar.provider.admin.AdminLocationInfoRequest
import com.procar.provider.admin.AdminLotMetadataRequest
import com.procar.provider.admin.AdminLotResponse
import com.procar.provider.admin.AdminSellerInfoRequest
import com.procar.provider.admin.AdminVehicleInfoRequest
import com.procar.provider.bid.ProviderBid
import com.procar.provider.lot.AuctionType
import com.procar.provider.lot.BodyType
import com.procar.provider.lot.DrivetrainType
import com.procar.provider.lot.FuelType
import com.procar.provider.lot.LotStatus
import com.procar.provider.lot.SellerType
import com.procar.provider.lot.TransmissionType
import com.procar.provider.lot.VehicleCondition
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.datetimepicker.DateTimePicker
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Route("")
class AdminView(
    private val gatewayClientService: GatewayClientService
) : VerticalLayout() {

    private val users = mutableListOf<String>()
    private val lots = mutableListOf<AdminLotResponse>()
    private val bids = mutableListOf<ProviderBid>()

    init {
        addClassName("admin-view")
        defaultHorizontalComponentAlignment = Alignment.CENTER

        add(H1("Procar Admin Dashboard"))

        // Load initial data
        loadData()

        add(createUserSection())
        add(createLotSection())
        add(createBidSection())
    }

    private fun createUserSection(): Component {
        val layout = VerticalLayout()
        layout.addClassName("section")

        layout.add(H2("Users"))

        val userGrid = Grid<String>()
        userGrid.setItems(users)
        userGrid.addColumn { user -> user }.setHeader("User")

        val addButton = Button("Add User") { showAddUserDialog() }
        val refreshButton = Button("Refresh") {
            loadUsers()
            userGrid.setItems(users)
            Notification.show("Users refreshed")
        }

        layout.add(userGrid, addButton, refreshButton)
        return layout
    }

    private fun createLotSection(): Component {
        val layout = VerticalLayout()
        layout.addClassName("section")

        layout.add(H2("Lots"))

        val lotGrid = Grid<AdminLotResponse>()
        lotGrid.setItems(lots)
        lotGrid.addColumn(AdminLotResponse::id).setHeader("ID")
        lotGrid.addColumn(AdminLotResponse::title).setHeader("Title")
        lotGrid.addColumn(AdminLotResponse::status).setHeader("Status")
        lotGrid.addColumn { it.auction.currentBid }.setHeader("Current Bid")

        val addButton = Button("Add Lot") { showAddLotDialog() }
        val refreshButton = Button("Refresh") {
            loadLots()
            lotGrid.setItems(lots)
            Notification.show("Lots refreshed")
        }

        layout.add(lotGrid, addButton, refreshButton)
        return layout
    }

    private fun createBidSection(): Component {
        val layout = VerticalLayout()
        layout.addClassName("section")

        layout.add(H2("Bids"))

        val bidGrid = Grid<ProviderBid>()
        bidGrid.setItems(bids)
        bidGrid.addColumn(ProviderBid::id).setHeader("ID")
        bidGrid.addColumn(ProviderBid::lotId).setHeader("Lot ID")
        bidGrid.addColumn(ProviderBid::bidderId).setHeader("User ID")
        bidGrid.addColumn(ProviderBid::amount).setHeader("Amount")

        val addButton = Button("Add Bid") { showAddBidDialog() }
        val refreshButton = Button("Refresh") {
            loadBids()
            bidGrid.setItems(bids)
            Notification.show("Bids refreshed")
        }

        layout.add(bidGrid, addButton, refreshButton)
        return layout
    }

    private fun showAddUserDialog() {
        val dialog = Dialog()
        dialog.setHeaderTitle("Add New User")

        val nameField = TextField("Name")
        val emailField = TextField("Email")
        val roleField = TextField("Role")

        val form = FormLayout(nameField, emailField, roleField)

        val saveButton = Button("Save") {
            val newUser = nameField.value
            users.add(newUser)
            Notification.show("User added: $newUser")
            dialog.close()
        }

        val cancelButton = Button("Cancel") { dialog.close() }

        dialog.add(form, saveButton, cancelButton)
        dialog.open()
    }

    private fun showAddLotDialog() {
        val dialog = Dialog()
        dialog.setHeaderTitle("Add New Lot")
        dialog.width = "900px"
        dialog.height = "800px"

        // Initialize all form fields
        initializeFormFields()

        // Add info text about required fields
        val infoText = com.vaadin.flow.component.html.Span("Fields marked with * are required")
        infoText.style.set("color", "var(--lumo-error-text-color)")
        infoText.style.set("font-size", "small")

        // Create main layout with 4 sections
        val mainLayout = VerticalLayout()
        mainLayout.setPadding(false)
        mainLayout.setSpacing(false)

        // Section 1: Basic Info
        val basicSection = createBasicInfoSection()

        // Section 2: Vehicle Details
        val vehicleSection = createVehicleSection()

        // Section 3: Auction Details
        val auctionSection = createAuctionSection()

        // Section 4: Location & Metadata
        val locationMetadataSection = createLocationMetadataSection()

        // Add all sections to main layout
        mainLayout.add(basicSection, vehicleSection, auctionSection, locationMetadataSection)

        // Add scroll wrapper for long form
        val scrollWrapper = com.vaadin.flow.component.orderedlayout.VerticalLayout()
        scrollWrapper.add(mainLayout)
        scrollWrapper.width = "100%"
        scrollWrapper.height = "650px"
        scrollWrapper.style.set("overflow", "auto")

        val saveButton = Button("Save") {
            createLot(dialog)
        }

        val cancelButton = Button("Cancel") { dialog.close() }

        val buttonLayout = HorizontalLayout(saveButton, cancelButton)
        buttonLayout.alignItems = Alignment.CENTER

        dialog.add(infoText, scrollWrapper, buttonLayout)
        dialog.open()
    }

    private fun showAddBidDialog() {
        val dialog = Dialog()
        dialog.setHeaderTitle("Add New Bid")

        val lotIdField = TextField("Lot ID")
        val userIdField = TextField("User ID")
        val amountField = TextField("Amount")

        val form = FormLayout(lotIdField, userIdField, amountField)

        val saveButton = Button("Save") {
            // TODO: Implement bid creation
            Notification.show("Bid creation to be implemented")
            dialog.close()
        }

        val cancelButton = Button("Cancel") { dialog.close() }

        dialog.add(form, saveButton, cancelButton)
        dialog.open()
    }

    private fun loadData() {
        loadUsers()
        loadLots()
        loadBids()
    }

    private fun loadUsers() {
        val currentUI = UI.getCurrent()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userList = gatewayClientService.getUsers().toList()
                currentUI.access {
                    users.clear()
                    users.addAll(userList)
                }
            } catch (error: Exception) {
                currentUI.access {
                    Notification.show("Error loading users: ${error.message}")
                }
            }
        }
    }

    private fun loadLots() {
        val currentUI = UI.getCurrent()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val lotList = gatewayClientService.getLots().toList()
                currentUI.access {
                    lots.clear()
                    lots.addAll(lotList)
                }
            } catch (error: Exception) {
                currentUI.access {
                    Notification.show("Error loading lots: ${error.message}")
                }
            }
        }
    }

    private fun loadBids() {
        // For now, load bids for first lot if available
        if (lots.isNotEmpty()) {
            val currentUI = UI.getCurrent()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bidList = gatewayClientService.getBidsForLot(lots.first().id).toList()
                    currentUI.access {
                        bids.clear()
                        bids.addAll(bidList)
                    }
                } catch (error: Exception) {
                    currentUI.access {
                        Notification.show("Error loading bids: ${error.message}")
                    }
                }
            }
        }
    }

    // Form field declarations for lot creation
    private lateinit var externalIdField: TextField
    private lateinit var titleField: TextField
    private lateinit var descriptionField: TextArea
    private lateinit var statusComboBox: ComboBox<LotStatus>

    // Vehicle fields
    private lateinit var vinField: TextField
    private lateinit var makeField: TextField
    private lateinit var modelField: TextField
    private lateinit var yearField: NumberField
    private lateinit var trimField: TextField
    private lateinit var bodyTypeField: ComboBox<BodyType>
    private lateinit var colorField: TextField
    private lateinit var interiorColorField: TextField
    private lateinit var mileageField: NumberField
    private lateinit var engineTypeField: TextField
    private lateinit var engineDisplacementField: NumberField
    private lateinit var engineCylindersField: NumberField
    private lateinit var engineHorsepowerField: NumberField
    private lateinit var engineTorqueField: NumberField
    private lateinit var transmissionComboBox: ComboBox<TransmissionType>
    private lateinit var drivetrainComboBox: ComboBox<DrivetrainType>
    private lateinit var fuelTypeComboBox: ComboBox<FuelType>
    private lateinit var conditionComboBox: ComboBox<VehicleCondition>

    // Auction fields
    private lateinit var currentBidField: NumberField
    private lateinit var startingBidField: NumberField
    private lateinit var reservePriceField: NumberField
    private lateinit var bidIncrementField: NumberField
    private lateinit var startTimePicker: DateTimePicker
    private lateinit var endTimePicker: DateTimePicker
    private lateinit var auctionTypeComboBox: ComboBox<AuctionType>
    private lateinit var buyItNowPriceField: NumberField

    // Location fields
    private lateinit var addressField: TextField
    private lateinit var cityField: TextField
    private lateinit var stateField: TextField
    private lateinit var zipCodeField: TextField
    private lateinit var countryField: TextField
    private lateinit var timezoneField: TextField

    // Metadata fields
    private lateinit var sellerIdField: TextField
    private lateinit var sellerNameField: TextField
    private lateinit var sellerTypeComboBox: ComboBox<SellerType>
    private lateinit var sellerRatingField: NumberField
    private lateinit var sellerTotalSalesField: NumberField
    private lateinit var sellerResponseRateField: NumberField

    private fun initializeFormFields() {
        // Basic info fields
        externalIdField = TextField("External ID")
        titleField = TextField("Title")
        descriptionField = TextArea("Description")
        statusComboBox = ComboBox("Status")
        statusComboBox.setItems(LotStatus.entries)
        statusComboBox.isClearButtonVisible = false

        // Vehicle fields
        vinField = TextField("VIN *")
        makeField = TextField("Make *")
        modelField = TextField("Model *")
        yearField = NumberField("Year *")
        trimField = TextField("Trim")
        bodyTypeField = ComboBox("Body Type *")
        bodyTypeField.setItems(BodyType.entries)
        bodyTypeField.isClearButtonVisible = false
        colorField = TextField("Color")
        interiorColorField = TextField("Interior Color")
        mileageField = NumberField("Mileage")
        engineTypeField = TextField("Engine Type *")
        engineDisplacementField = NumberField("Engine Displacement (L)")
        engineCylindersField = NumberField("Engine Cylinders")
        engineHorsepowerField = NumberField("Horsepower")
        engineTorqueField = NumberField("Torque")
        transmissionComboBox = ComboBox("Transmission *")
        transmissionComboBox.setItems(TransmissionType.entries)
        transmissionComboBox.isClearButtonVisible = false
        drivetrainComboBox = ComboBox("Drivetrain *")
        drivetrainComboBox.setItems(DrivetrainType.entries)
        drivetrainComboBox.isClearButtonVisible = false
        fuelTypeComboBox = ComboBox("Fuel Type *")
        fuelTypeComboBox.setItems(FuelType.entries)
        fuelTypeComboBox.isClearButtonVisible = false
        conditionComboBox = ComboBox("Condition *")
        conditionComboBox.setItems(VehicleCondition.entries)
        conditionComboBox.isClearButtonVisible = false

        // Auction fields
        currentBidField = NumberField("Current Bid *")
        startingBidField = NumberField("Starting Bid *")
        reservePriceField = NumberField("Reserve Price")
        bidIncrementField = NumberField("Bid Increment *")
        startTimePicker = DateTimePicker("Start Time *")
        endTimePicker = DateTimePicker("End Time *")
        auctionTypeComboBox = ComboBox("Auction Type *")
        auctionTypeComboBox.setItems(AuctionType.entries)
        auctionTypeComboBox.isClearButtonVisible = false
        buyItNowPriceField = NumberField("Buy It Now Price")

        // Location fields
        addressField = TextField("Address *")
        cityField = TextField("City *")
        stateField = TextField("State *")
        zipCodeField = TextField("ZIP Code *")
        countryField = TextField("Country *")
        timezoneField = TextField("Timezone *")

        // Metadata fields
        sellerIdField = TextField("Seller ID *")
        sellerNameField = TextField("Seller Name *")
        sellerTypeComboBox = ComboBox("Seller Type *")
        sellerTypeComboBox.setItems(SellerType.entries)
        sellerTypeComboBox.isClearButtonVisible = false
        sellerRatingField = NumberField("Seller Rating")
        sellerTotalSalesField = NumberField("Total Sales")
        sellerResponseRateField = NumberField("Response Rate")
    }

    private fun createBasicInfoSection(): Component {
        val section = com.vaadin.flow.component.orderedlayout.VerticalLayout()
        section.addClassName("form-section")

        val sectionTitle = H2("Basic Information")
        sectionTitle.style.set("color", "var(--lumo-primary-color)")
        section.add(sectionTitle)

        val formLayout = FormLayout(
            externalIdField, titleField, descriptionField, statusComboBox
        )
        formLayout.width = "100%"
        section.add(formLayout)

        return section
    }

    private fun createVehicleSection(): Component {
        val section = com.vaadin.flow.component.orderedlayout.VerticalLayout()
        section.addClassName("form-section")

        val sectionTitle = H2("Vehicle Details")
        sectionTitle.style.set("color", "var(--lumo-primary-color)")
        section.add(sectionTitle)

        val formLayout = FormLayout(
            vinField,
            makeField,
            modelField,
            yearField,
            trimField,
            bodyTypeField,
            colorField,
            interiorColorField,
            mileageField,
            engineTypeField,
            engineDisplacementField,
            engineCylindersField,
            engineHorsepowerField,
            engineTorqueField,
            transmissionComboBox,
            drivetrainComboBox,
            fuelTypeComboBox,
            conditionComboBox
        )
        formLayout.width = "100%"
        section.add(formLayout)

        return section
    }

    private fun createAuctionSection(): Component {
        val section = com.vaadin.flow.component.orderedlayout.VerticalLayout()
        section.addClassName("form-section")

        val sectionTitle = H2("Auction Details")
        sectionTitle.style.set("color", "var(--lumo-primary-color)")
        section.add(sectionTitle)

        val formLayout = FormLayout(
            currentBidField, startingBidField, reservePriceField, bidIncrementField,
            startTimePicker, endTimePicker, auctionTypeComboBox, buyItNowPriceField
        )
        formLayout.width = "100%"
        section.add(formLayout)

        // Set default values
        startTimePicker.value = LocalDateTime.now().plusDays(1)
        endTimePicker.value = LocalDateTime.now().plusDays(7)

        return section
    }

    private fun createLocationMetadataSection(): Component {
        val section = com.vaadin.flow.component.orderedlayout.VerticalLayout()
        section.addClassName("form-section")

        val sectionTitle = H2("Location & Seller Information")
        sectionTitle.style.set("color", "var(--lumo-primary-color)")
        section.add(sectionTitle)

        // Create two-column layout for location and metadata
        val twoColumnLayout = com.vaadin.flow.component.orderedlayout.HorizontalLayout()
        twoColumnLayout.width = "100%"

        // Location column
        val locationColumn = com.vaadin.flow.component.orderedlayout.VerticalLayout()
        locationColumn.addClassName("form-column")
        val locationTitle = H3("Location")
        locationTitle.style.set("color", "var(--lumo-secondary-color)")
        locationColumn.add(locationTitle)

        val locationForm = FormLayout(
            addressField, cityField, stateField, zipCodeField, countryField, timezoneField
        )
        locationForm.width = "100%"
        locationColumn.add(locationForm)

        // Metadata column
        val metadataColumn = com.vaadin.flow.component.orderedlayout.VerticalLayout()
        metadataColumn.addClassName("form-column")
        val metadataTitle = H3("Seller Information")
        metadataTitle.style.set("color", "var(--lumo-secondary-color)")
        metadataColumn.add(metadataTitle)

        val metadataForm = FormLayout(
            sellerIdField, sellerNameField, sellerTypeComboBox,
            sellerRatingField, sellerTotalSalesField, sellerResponseRateField
        )
        metadataForm.width = "100%"
        metadataColumn.add(metadataForm)

        twoColumnLayout.add(locationColumn, metadataColumn)
        section.add(twoColumnLayout)

        // Set default timezone
        timezoneField.value = "UTC"

        return section
    }

    private fun generateAutoTitle(): String {
        val year = yearField.value?.toInt() ?: return ""
        val make = makeField.value ?: return ""
        val model = modelField.value ?: return ""

        val titleParts = mutableListOf<String>()
        titleParts.add(year.toString())
        titleParts.add(make)
        titleParts.add(model)

        engineDisplacementField.value
            ?.takeIf { it > 0 }
            ?.let { titleParts.add(String.format("%.1f", it)) }

        engineHorsepowerField.value?.toInt()?.let {
            titleParts.add("${it}HP")
        }

        return titleParts.joinToString(" ")
    }

    private fun createLot(dialog: Dialog) {
        try {
            // Collect all validation errors
            val validationErrors = mutableListOf<String>()

            // Validate required fields
            if (makeField.value.isNullOrBlank()) validationErrors.add("Make is required")
            if (modelField.value.isNullOrBlank()) validationErrors.add("Model is required")
            if (yearField.value == null) validationErrors.add("Year is required")
            if (bodyTypeField.value == null) validationErrors.add("Body Type is required")
            if (engineTypeField.value.isNullOrBlank()) validationErrors.add("Engine Type is required")
            if (transmissionComboBox.value == null) validationErrors.add("Transmission is required")
            if (drivetrainComboBox.value == null) validationErrors.add("Drivetrain is required")
            if (fuelTypeComboBox.value == null) validationErrors.add("Fuel Type is required")
            if (conditionComboBox.value == null) validationErrors.add("Condition is required")

            if (currentBidField.value == null) validationErrors.add("Current Bid is required")
            if (startingBidField.value == null) validationErrors.add("Starting Bid is required")
            if (bidIncrementField.value == null) validationErrors.add("Bid Increment is required")
            if (startTimePicker.value == null) validationErrors.add("Start Time is required")
            if (endTimePicker.value == null) validationErrors.add("End Time is required")
            if (auctionTypeComboBox.value == null) validationErrors.add("Auction Type is required")

            if (addressField.value.isNullOrBlank()) validationErrors.add("Address is required")
            if (cityField.value.isNullOrBlank()) validationErrors.add("City is required")
            if (stateField.value.isNullOrBlank()) validationErrors.add("State is required")
            if (zipCodeField.value.isNullOrBlank()) validationErrors.add("ZIP Code is required")
            if (countryField.value.isNullOrBlank()) validationErrors.add("Country is required")
            if (timezoneField.value.isNullOrBlank()) validationErrors.add("Timezone is required")

            if (sellerIdField.value.isNullOrBlank()) validationErrors.add("Seller ID is required")
            if (sellerNameField.value.isNullOrBlank()) validationErrors.add("Seller Name is required")
            if (sellerTypeComboBox.value == null) validationErrors.add("Seller Type is required")

            // Validate business logic only if required fields are present
            if (startTimePicker.value != null && endTimePicker.value != null) {
                if (startTimePicker.value!!.isAfter(endTimePicker.value!!)) {
                    validationErrors.add("Start time must be before end time")
                }
            }

            if (startingBidField.value != null && startingBidField.value!! <= 0) {
                validationErrors.add("Starting bid must be greater than 0")
            }

            if (bidIncrementField.value != null && bidIncrementField.value!! <= 0) {
                validationErrors.add("Bid increment must be greater than 0")
            }

            if (reservePriceField.value != null && startingBidField.value != null &&
                reservePriceField.value!! < startingBidField.value!!
            ) {
                validationErrors.add("Reserve price cannot be less than starting bid")
            }

            // Show all validation errors if any exist
            if (validationErrors.isNotEmpty()) {
                val errorMessage = if (validationErrors.size == 1) {
                    validationErrors.first()
                } else {
                    "Please fix the following issues:\n${validationErrors.joinToString("\n") { "• $it" }}"
                }
                Notification.show(errorMessage)
                return
            }

            val finalTitle = if (titleField.value.isNullOrBlank()) {
                generateAutoTitle()
            } else {
                titleField.value
            }

            val request = AdminCreateLotRequest(
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
                location = AdminLocationInfoRequest(
                    address = addressField.value,
                    city = cityField.value,
                    state = stateField.value,
                    zipCode = zipCodeField.value,
                    country = countryField.value,
                    timezone = timezoneField.value
                ),
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

            // Create the lot using coroutine
            val currentUI = UI.getCurrent()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val createdLot = gatewayClientService.createLot(request)
                    currentUI.access {
                        Notification.show("Lot created successfully: ${createdLot.id}")
                        dialog.close()
                        loadLots() // Refresh the lots list
                    }
                } catch (error: Exception) {
                    currentUI.access {
                        Notification.show("Error creating lot: ${error.message}")
                    }
                }
            }

        } catch (e: Exception) {
            Notification.show("Validation error: ${e.message}")
        }
    }
}
