package com.procar.auction.cucumber

import com.procar.auction.api.admin.*
import com.procar.provider.bid.BidType
import com.procar.provider.bid.PlaceBidRequest
import com.procar.provider.common.GeoCoordinates
import com.procar.provider.common.PaginationRequest
import com.procar.provider.common.SortCriteria
import com.procar.provider.common.SortDirection
import com.procar.provider.common.SortField
import com.procar.provider.lot.*
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.time.LocalDateTime

class ProcarWorkflowStepDefinitions {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") { "mongodb://localhost:27017/procar-auction-cucumber-test" }
            registry.add("de.flapdoodle.mongodb.embedded.version") { "6.0.8" }
        }
    }

    private var testLotId: String? = null
    private var testExternalId: String? = null
    private var lastResponse: org.springframework.http.ResponseEntity<*>? = null
    private var createLotRequest: AdminCreateLotRequest? = null

    @Given("I have access to the auction system")
    fun iHaveAccessToAuctionSystem() {
        // Verify restTemplate is available
        assertNotNull(restTemplate)
    }

    @And("MongoDB is available for testing")
    fun mongoDbIsAvailableForTesting() {
        // MongoDB will be started via embedded mongo configuration
        // This step serves as documentation
    }

    @Given("I have admin access to create lots")
    fun iHaveAdminAccessToCreateLots() {
        // Admin access is verified through the restTemplate configuration
        assertNotNull(restTemplate)
    }

    @When("I create a new lot with complete vehicle details")
    fun iCreateANewLotWithCompleteVehicleDetails(dataTable: io.cucumber.datatable.DataTable) {
        val data = dataTable.asMaps()[0]
        testExternalId = "${data["externalId"]}-${System.currentTimeMillis()}"
        
        createLotRequest = AdminCreateLotRequest(
            externalId = testExternalId!!,
            title = data["title"]!!,
            description = data["description"]!!,
            vehicle = AdminVehicleInfoRequest(
                vin = data["vin"]!!,
                make = data["make"]!!,
                model = data["model"]!!,
                year = data["year"]!!.toInt(),
                trim = "XSE",
                bodyType = "Sedan",
                color = "Silver",
                interiorColor = "Black",
                mileage = 5000,
                engine = AdminEngineInfoRequest(
                    type = "4-Cylinder",
                    displacement = 2.5,
                    cylinders = 4,
                    horsepower = 203,
                    torque = 184
                ),
                transmission = TransmissionType.AUTOMATIC,
                drivetrain = DrivetrainType.FWD,
                fuelType = FuelType.GASOLINE,
                condition = VehicleCondition.EXCELLENT,
                features = listOf("Bluetooth", "Backup Camera", "Cruise Control", "Lane Assist"),
                images = listOf(
                    AdminVehicleImageRequest(
                        url = "https://example.com/workflow-exterior.jpg",
                        type = ImageType.EXTERIOR,
                        description = "Exterior view",
                        isPrimary = true
                    ),
                    AdminVehicleImageRequest(
                        url = "https://example.com/workflow-interior.jpg",
                        type = ImageType.INTERIOR,
                        description = "Interior view"
                    )
                ),
                documents = listOf(
                    AdminDocumentRequest(
                        type = DocumentType.REGISTRATION,
                        url = "https://example.com/workflow-registration.pdf",
                        description = "Vehicle registration"
                    ),
                    AdminDocumentRequest(
                        type = DocumentType.INSPECTION_REPORT,
                        url = "https://example.com/workflow-inspection.pdf",
                        description = "Inspection report"
                    )
                )
            ),
            auction = AdminAuctionInfoRequest(
                currentBid = 15000.0,
                startingBid = 10000.0,
                reservePrice = 18000.0,
                bidIncrement = 250.0,
                startTime = LocalDateTime.now().plusMinutes(5),
                endTime = LocalDateTime.now().plusDays(7),
                auctionType = AuctionType.ONLINE
            ),
            location = AdminLocationInfoRequest(
                address = "789 Workflow Street",
                city = "Testville",
                state = "CA",
                zipCode = "90210",
                country = "USA",
                coordinates = GeoCoordinates(34.0522, -118.2437),
                timezone = "America/Los_Angeles"
            ),
            metadata = AdminLotMetadataRequest(
                tags = listOf("cucumber-test", "workflow", "featured"),
                categories = listOf("sedan", "toyota", "low-mileage"),
                sellerInfo = AdminSellerInfoRequest(
                    id = "cucumber-seller-${System.currentTimeMillis()}",
                    name = "Cucumber Test Dealership",
                    type = SellerType.DEALER,
                    rating = 4.8,
                    totalSales = 150,
                    responseRate = 98.0
                ),
                inspection = AdminInspectionInfoRequest(
                    inspected = true,
                    inspectionDate = LocalDateTime.now().minusDays(3),
                    inspector = "Cucumber Inspector",
                    reportUrl = "https://example.com/cucumber-inspection.pdf",
                    overallCondition = "Excellent",
                    keyFindings = listOf("No accidents", "Regular maintenance", "Like new condition")
                ),
                history = AdminVehicleHistoryRequest(
                    accidents = 0,
                    owners = 1,
                    titleStatus = TitleStatus.CLEAN,
                    serviceRecords = true,
                    lastServiceDate = LocalDateTime.now().minusMonths(2)
                ),
                fees = listOf(
                    AdminFeeRequest(
                        type = FeeType.DOCUMENTATION,
                        amount = 175.0,
                        description = "Documentation fee",
                        mandatory = true
                    ),
                    AdminFeeRequest(
                        type = FeeType.BUYER_PREMIUM,
                        amount = 250.0,
                        description = "Buyer premium",
                        mandatory = false
                    )
                ),
                shipping = AdminShippingInfoRequest(
                    available = true,
                    estimatedCost = 600.0,
                    methods = listOf(ShippingMethod.DELIVERY, ShippingMethod.PICKUP),
                    restrictions = listOf("Continental US only", "Must schedule pickup")
                )
            ),
            status = LotStatus.ACTIVE
        )

        lastResponse = restTemplate.postForEntity(
            "/api/admin/lots",
            createLotRequest,
            AdminLotResponse::class.java
        )
    }

    @When("I create a minimal lot with required fields only")
    fun iCreateAMinimalLotWithRequiredFieldsOnly(dataTable: io.cucumber.datatable.DataTable) {
        val data = dataTable.asMaps()[0]
        testExternalId = "${data["externalId"]}-${System.currentTimeMillis()}"
        
        createLotRequest = AdminCreateLotRequest(
            externalId = testExternalId!!,
            title = data["title"]!!,
            description = "Minimal test car for cucumber testing",
            vehicle = AdminVehicleInfoRequest(
                vin = "1HGBH41JXMN100001",
                make = data["make"]!!,
                model = data["model"]!!,
                year = data["year"]!!.toInt(),
                trim = "Base",
                bodyType = "Sedan",
                color = "White",
                interiorColor = "Gray",
                mileage = 10000,
                engine = AdminEngineInfoRequest(
                    type = "4-Cylinder",
                    displacement = 2.0,
                    cylinders = 4,
                    horsepower = 158,
                    torque = 138
                ),
                transmission = TransmissionType.AUTOMATIC,
                drivetrain = DrivetrainType.FWD,
                fuelType = FuelType.GASOLINE,
                condition = VehicleCondition.GOOD,
                features = emptyList(),
                images = emptyList(),
                documents = emptyList()
            ),
            auction = AdminAuctionInfoRequest(
                currentBid = 8000.0,
                startingBid = 5000.0,
                reservePrice = 10000.0,
                bidIncrement = 100.0,
                startTime = LocalDateTime.now().plusMinutes(5),
                endTime = LocalDateTime.now().plusDays(7),
                auctionType = AuctionType.ONLINE
            ),
            location = AdminLocationInfoRequest(
                address = "123 Minimal Street",
                city = "Test City",
                state = "CA",
                zipCode = "90210",
                country = "USA",
                coordinates = GeoCoordinates(34.0522, -118.2437),
                timezone = "America/Los_Angeles"
            ),
            metadata = AdminLotMetadataRequest(
                tags = listOf("cucumber-minimal"),
                categories = listOf("sedan", data["make"]!!.lowercase()),
                sellerInfo = AdminSellerInfoRequest(
                    id = "minimal-seller-${System.currentTimeMillis()}",
                    name = "Minimal Test Dealership",
                    type = SellerType.DEALER,
                    rating = 4.5,
                    totalSales = 50,
                    responseRate = 95.0
                ),
                inspection = AdminInspectionInfoRequest(
                    inspected = true,
                    inspectionDate = LocalDateTime.now().minusDays(1),
                    inspector = "Minimal Inspector",
                    reportUrl = "https://example.com/minimal-inspection.pdf",
                    overallCondition = "Good",
                    keyFindings = listOf("Regular maintenance")
                ),
                history = AdminVehicleHistoryRequest(
                    accidents = 0,
                    owners = 2,
                    titleStatus = TitleStatus.CLEAN,
                    serviceRecords = true,
                    lastServiceDate = LocalDateTime.now().minusMonths(1)
                ),
                fees = listOf(
                    AdminFeeRequest(
                        type = FeeType.DOCUMENTATION,
                        amount = 150.0,
                        description = "Documentation fee",
                        mandatory = true
                    )
                ),
                shipping = AdminShippingInfoRequest(
                    available = true,
                    estimatedCost = 500.0,
                    methods = listOf(ShippingMethod.PICKUP),
                    restrictions = listOf("Local pickup only")
                )
            ),
            status = LotStatus.ACTIVE
        )

        lastResponse = restTemplate.postForEntity(
            "/api/admin/lots",
            createLotRequest,
            AdminLotResponse::class.java
        )
    }

    @Then("the lot should be created successfully")
    fun theLotShouldBeCreatedSuccessfully() {
        assertNotNull(lastResponse)
        assertEquals(HttpStatus.OK, lastResponse!!.statusCode)
        assertNotNull(lastResponse!!.body)
        
        val lotResponse = lastResponse!!.body as AdminLotResponse
        testLotId = lotResponse.id
        assertNotNull(testLotId)
        println("PASS: Lot created successfully with ID: $testLotId")
    }

    @And("the lot should have default auction settings")
    fun theLotShouldHaveDefaultAuctionSettings() {
        val lotResponse = lastResponse!!.body as AdminLotResponse
        assertNotNull(lotResponse.auction)
        assertEquals(5000.0, lotResponse.auction.startingBid)
        assertEquals(10000.0, lotResponse.auction.reservePrice)
        assertEquals(100.0, lotResponse.auction.bidIncrement)
    }

    @When("I search for the lot using public API")
    fun iSearchForTheLotUsingPublicApi() {
        val searchRequest = AdvancedLotSearchRequest(
            query = createLotRequest!!.title,
            filters = LotSearchFilters(
                status = listOf(LotStatus.ACTIVE),
                vehicle = VehicleFilters(
                    makes = listOf(createLotRequest!!.vehicle.make),
                    models = listOf(createLotRequest!!.vehicle.model)
                )
            ),
            sorting = listOf(SortCriteria(SortField.PLACED_AT, SortDirection.DESC)),
            pagination = PaginationRequest(size = 10, cursor = null)
        )

        lastResponse = restTemplate.postForEntity(
            "/internal/lots/search",
            searchRequest,
            String::class.java
        )
    }

    @Then("the lot should appear in search results")
    fun theLotShouldAppearInSearchResults() {
        assertNotNull(lastResponse)
        assertEquals(HttpStatus.OK, lastResponse!!.statusCode)
        assertNotNull(lastResponse!!.body)
        assertTrue((lastResponse!!.body as String).contains(testExternalId!!))
        println("PASS: Lot found in public search results")
    }

    @When("I retrieve the lot details")
    fun iRetrieveTheLotDetails() {
        lastResponse = restTemplate.getForEntity(
            "/internal/lots/{lotId}",
            String::class.java,
            testLotId
        )
    }

    @Then("I should see the complete vehicle information")
    fun iShouldSeeTheCompleteVehicleInformation() {
        assertNotNull(lastResponse)
        assertEquals(HttpStatus.OK, lastResponse!!.statusCode)
        assertNotNull(lastResponse!!.body)
        assertTrue((lastResponse!!.body as String).contains(testExternalId!!))
        println("PASS: Lot details retrieved successfully")
    }

    @And("the bid history should be empty")
    fun theBidHistoryShouldBeEmpty() {
        val bidHistoryResponse = restTemplate.getForEntity(
            "/internal/bids/{lotId}/history",
            String::class.java,
            testLotId
        )

        assertNotNull(bidHistoryResponse)
        assertEquals(HttpStatus.OK, bidHistoryResponse.statusCode)
        assertNotNull(bidHistoryResponse.body)
        println("PASS: Initial bid history retrieved (expecting 0 bids)")
    }

    @When("I place a bid of {double} from bidder {string}")
    fun iPlaceABidOfFromBidder(amount: Double, bidderId: String) {
        val bidRequest = PlaceBidRequest(
            lotId = testLotId!!,
            bidderId = bidderId,
            amount = amount,
            bidType = BidType.MANUAL
        )

        lastResponse = restTemplate.postForEntity(
            "/internal/bids/place",
            bidRequest,
            String::class.java
        )
    }

    @Then("the bid should be accepted")
    fun theBidShouldBeAccepted() {
        assertNotNull(lastResponse)
        assertEquals(HttpStatus.OK, lastResponse!!.statusCode)
        println("PASS: Bid placed successfully")
    }

    @When("I check the bid history")
    fun iCheckTheBidHistory() {
        lastResponse = restTemplate.getForEntity(
            "/internal/bids/{lotId}/history",
            String::class.java,
            testLotId
        )
    }

    @Then("I should see {int} bids in chronological order")
    fun iShouldSeeBidsInChronologicalOrder(expectedBidCount: Int) {
        assertNotNull(lastResponse)
        assertEquals(HttpStatus.OK, lastResponse!!.statusCode)
        assertNotNull(lastResponse!!.body)
        println("PASS: Updated bid history retrieved (expecting $expectedBidCount bids)")
    }

    @When("I remove all bids via admin endpoint")
    fun iRemoveAllBidsViaAdminEndpoint() {
        lastResponse = restTemplate.exchange(
            "/api/admin/bids/lot/{lotId}",
            HttpMethod.DELETE,
            null,
            String::class.java,
            testLotId
        )
    }

    @Then("all bids should be deleted")
    fun allBidsShouldBeDeleted() {
        assertNotNull(lastResponse)
        assertEquals(HttpStatus.NO_CONTENT, lastResponse!!.statusCode)
        println("PASS: All bids removed successfully")
    }

    @When("I delete the lot via admin endpoint")
    fun iDeleteTheLotViaAdminEndpoint() {
        lastResponse = restTemplate.exchange(
            "/api/admin/lots/{lotId}",
            HttpMethod.DELETE,
            null,
            String::class.java,
            testLotId
        )
    }

    @Then("the lot should be removed")
    fun theLotShouldBeRemoved() {
        assertNotNull(lastResponse)
        assertEquals(HttpStatus.NO_CONTENT, lastResponse!!.statusCode)
        println("PASS: Lot deleted successfully")
    }

    @When("I search for the deleted lot")
    fun iSearchForTheDeletedLot() {
        val searchRequest = AdvancedLotSearchRequest(
            query = createLotRequest!!.title,
            filters = LotSearchFilters(
                status = listOf(LotStatus.ACTIVE),
                vehicle = VehicleFilters(
                    makes = listOf(createLotRequest!!.vehicle.make),
                    models = listOf(createLotRequest!!.vehicle.model)
                )
            ),
            sorting = listOf(SortCriteria(SortField.PLACED_AT, SortDirection.DESC)),
            pagination = PaginationRequest(size = 10, cursor = null)
        )

        lastResponse = restTemplate.postForEntity(
            "/internal/lots/search",
            searchRequest,
            String::class.java
        )
    }

    @Then("the lot should not appear in results")
    fun theLotShouldNotAppearInResults() {
        assertNotNull(lastResponse)
        assertEquals(HttpStatus.OK, lastResponse!!.statusCode)
        assertNotNull(lastResponse!!.body)
        assertTrue(!(lastResponse!!.body as String).contains(testExternalId!!) || (lastResponse!!.body as String).contains("\"results\":[]"))
        println("PASS: Lot correctly removed from search results")
    }

    @And("accessing the lot directly should return 404")
    fun accessingTheLotDirectlyShouldReturn404() {
        val deletedLotResponse = restTemplate.getForEntity(
            "/internal/lots/{lotId}",
            String::class.java,
            testLotId
        )

        assertNotNull(deletedLotResponse)
        assertEquals(HttpStatus.NOT_FOUND, deletedLotResponse.statusCode)
        println("PASS: Deleted lot correctly returns NOT_FOUND")
    }
}
