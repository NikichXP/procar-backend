package com.procar.auction.document

import com.procar.provider.common.GeoCoordinates
import com.procar.provider.lot.*
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Document(collection = "lots")
data class LotEntity(
    @Id
    @OptIn(ExperimentalUuidApi::class)
    val id: String = Uuid.generateV7().toString(),

    @Field("provider_id")
    var providerId: String = "procar",

    @Field("provider_name")
    var providerName: String = "Procar",

    @Field("external_id")
    var externalId: String,

    @Field("title")
    var title: String,

    @Field("description")
    var description: String,

    @Field("vehicle")
    var vehicle: VehicleInfoDocument,

    @Field("auction")
    var auction: AuctionInfoDocument?,

    @Field("location")
    var location: LocationInfoDocument,

    @Field("metadata")
    var metadata: LotMetadataDocument,

    @Field("status")
    var status: LotStatus,

    @Field("broker_org_id")
    var brokerOrgId: String? = null,

    @Field("created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Field("updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Field("lot_type")
    var lotType: LotType = LotType.AUCTION,

    @Field("buyout_price")
    var buyoutPrice: Double? = null,

    @Field("brand")
    var brand: LotBrand = LotBrand.PARTNER,
)

@Document
data class VehicleInfoDocument(
    @Field("vin")
    var vin: String?,
    
    @Field("make")
    var make: String,
    
    @Field("model")
    var model: String,
    
    @Field("year")
    var year: Int,
    
    @Field("trim")
    var trim: String?,
    
    @Field("body_type")
    var bodyType: String,
    
    @Field("color")
    var color: String?,
    
    @Field("interior_color")
    var interiorColor: String?,
    
    @Field("mileage")
    var mileage: Int?,
    
    @Field("engine")
    var engine: EngineInfoDocument,
    
    @Field("transmission")
    var transmission: TransmissionType,
    
    @Field("drivetrain")
    var drivetrain: DrivetrainType,
    
    @Field("fuel_type")
    var fuelType: FuelType,
    
    @Field("condition")
    var condition: VehicleCondition,
    
    @Field("features")
    var features: List<String>,
    
    @Field("images")
    var images: List<VehicleImageDocument>,
    
    @Field("documents")
    var documents: List<DocumentDocument>,
    
    @Field("damage")
    var damage: List<DamageInfoDocument>?
)

@Document
data class EngineInfoDocument(
    @Field("type")
    var type: String,
    
    @Field("displacement")
    var displacement: Double?,
    
    @Field("cylinders")
    var cylinders: Int?,
    
    @Field("horsepower")
    var horsepower: Int?,
    
    @Field("torque")
    var torque: Int?
)

@Document
data class VehicleImageDocument(
    @Field("url")
    var url: String,
    
    @Field("type")
    var type: ImageType,
    
    @Field("description")
    var description: String?,
    
    @Field("is_primary")
    var isPrimary: Boolean
)

@Document
data class DocumentDocument(
    @Field("type")
    var type: DocumentType,
    
    @Field("url")
    var url: String,
    
    @Field("description")
    var description: String?
)

@Document
data class DamageInfoDocument(
    @Field("area")
    var area: String,
    
    @Field("severity")
    var severity: DamageSeverity,
    
    @Field("description")
    var description: String,
    
    @Field("estimated_repair_cost")
    var estimatedRepairCost: Double?
)

@Document
data class AuctionInfoDocument(
    @Field("current_bid")
    var currentBid: Double,
    
    @Field("starting_bid")
    var startingBid: Double,
    
    @Field("reserve_price")
    var reservePrice: Double?,
    
    @Field("bid_increment")
    var bidIncrement: Double,
    
    @Field("total_bids")
    var totalBids: Int = 0,
    
    @Field("start_time")
    var startTime: LocalDateTime,
    
    @Field("end_time")
    var endTime: LocalDateTime,
    
    @Field("time_remaining")
    var timeRemaining: Long?
) {
    fun isActiveAt(date: LocalDateTime): Boolean {
        return startTime.isBefore(date) && endTime.isAfter(date)
    }
}

@Document
data class LocationInfoDocument(
    @Field("address")
    var address: String,
    
    @Field("city")
    var city: String,
    
    @Field("state")
    var state: String,
    
    @Field("zip_code")
    var zipCode: String,
    
    @Field("country")
    var country: String,
    
    @Field("coordinates")
    var coordinates: GeoCoordinates?,
    
    @Field("timezone")
    var timezone: String
)

@Document
data class LotMetadataDocument(
    @Field("tags")
    var tags: List<String>,
    
    @Field("categories")
    var categories: List<String>,
    
    @Field("seller_info")
    var sellerInfo: SellerInfoDocument,
    
    @Field("inspection")
    var inspection: InspectionInfoDocument?,
    
    @Field("history")
    var history: VehicleHistoryDocument?,
    
    @Field("fees")
    var fees: List<FeeDocument>,
    
    @Field("shipping")
    var shipping: ShippingInfoDocument?
)

@Document
data class SellerInfoDocument(
    @Field("id")
    var id: String,
    
    @Field("name")
    var name: String,
    
    @Field("type")
    var type: SellerType,
    
    @Field("rating")
    var rating: Double?,
    
    @Field("total_sales")
    var totalSales: Int?,
    
    @Field("response_rate")
    var responseRate: Double?
)

@Document
data class InspectionInfoDocument(
    @Field("inspected")
    var inspected: Boolean,
    
    @Field("inspection_date")
    var inspectionDate: LocalDateTime?,
    
    @Field("inspector")
    var inspector: String?,
    
    @Field("report_url")
    var reportUrl: String?,
    
    @Field("overall_condition")
    var overallCondition: String?,
    
    @Field("key_findings")
    var keyFindings: List<String>
)

@Document
data class VehicleHistoryDocument(
    @Field("accidents")
    var accidents: Int,
    
    @Field("owners")
    var owners: Int,
    
    @Field("title_status")
    var titleStatus: TitleStatus,
    
    @Field("service_records")
    var serviceRecords: Boolean,
    
    @Field("last_service_date")
    var lastServiceDate: LocalDateTime?
)

@Document
data class FeeDocument(
    @Field("type")
    var type: FeeType,
    
    @Field("amount")
    var amount: Double,
    
    @Field("description")
    var description: String,
    
    @Field("mandatory")
    var mandatory: Boolean
)

@Document
data class ShippingInfoDocument(
    @Field("available")
    var available: Boolean,
    
    @Field("estimated_cost")
    var estimatedCost: Double?,
    
    @Field("methods")
    var methods: List<ShippingMethod>,
    
    @Field("restrictions")
    var restrictions: List<String>
)
