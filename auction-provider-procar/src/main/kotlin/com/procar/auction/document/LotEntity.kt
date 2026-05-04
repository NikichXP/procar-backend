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
    val providerId: String = "procar",

    @Field("provider_name")
    val providerName: String = "Procar",

    @Field("external_id")
    val externalId: String,

    @Field("title")
    val title: String,

    @Field("description")
    val description: String,

    @Field("vehicle")
    val vehicle: VehicleInfoDocument,

    @Field("auction")
    val auction: AuctionInfoDocument?,

    @Field("location")
    val location: LocationInfoDocument,

    @Field("metadata")
    val metadata: LotMetadataDocument,

    @Field("status")
    val status: LotStatus,

    @Field("broker_org_id")
    val brokerOrgId: String? = null,

    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Field("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Field("lot_type")
    val lotType: LotType = LotType.AUCTION,

    @Field("buyout_price")
    val buyoutPrice: Double? = null,
)

@Document
data class VehicleInfoDocument(
    @Field("vin")
    val vin: String?,
    
    @Field("make")
    val make: String,
    
    @Field("model")
    val model: String,
    
    @Field("year")
    val year: Int,
    
    @Field("trim")
    val trim: String?,
    
    @Field("body_type")
    val bodyType: String,
    
    @Field("color")
    val color: String?,
    
    @Field("interior_color")
    val interiorColor: String?,
    
    @Field("mileage")
    val mileage: Int?,
    
    @Field("engine")
    val engine: EngineInfoDocument,
    
    @Field("transmission")
    val transmission: TransmissionType,
    
    @Field("drivetrain")
    val drivetrain: DrivetrainType,
    
    @Field("fuel_type")
    val fuelType: FuelType,
    
    @Field("condition")
    val condition: VehicleCondition,
    
    @Field("features")
    val features: List<String>,
    
    @Field("images")
    val images: List<VehicleImageDocument>,
    
    @Field("documents")
    val documents: List<DocumentDocument>,
    
    @Field("damage")
    val damage: List<DamageInfoDocument>?
)

@Document
data class EngineInfoDocument(
    @Field("type")
    val type: String,
    
    @Field("displacement")
    val displacement: Double?,
    
    @Field("cylinders")
    val cylinders: Int?,
    
    @Field("horsepower")
    val horsepower: Int?,
    
    @Field("torque")
    val torque: Int?
)

@Document
data class VehicleImageDocument(
    @Field("url")
    val url: String,
    
    @Field("type")
    val type: ImageType,
    
    @Field("description")
    val description: String?,
    
    @Field("is_primary")
    val isPrimary: Boolean
)

@Document
data class DocumentDocument(
    @Field("type")
    val type: DocumentType,
    
    @Field("url")
    val url: String,
    
    @Field("description")
    val description: String?
)

@Document
data class DamageInfoDocument(
    @Field("area")
    val area: String,
    
    @Field("severity")
    val severity: DamageSeverity,
    
    @Field("description")
    val description: String,
    
    @Field("estimated_repair_cost")
    val estimatedRepairCost: Double?
)

@Document
data class AuctionInfoDocument(
    @Field("current_bid")
    var currentBid: Double,
    
    @Field("starting_bid")
    val startingBid: Double,
    
    @Field("reserve_price")
    val reservePrice: Double?,
    
    @Field("bid_increment")
    val bidIncrement: Double,
    
    @Field("total_bids")
    var totalBids: Int = 0,
    
    @Field("start_time")
    val startTime: LocalDateTime,
    
    @Field("end_time")
    val endTime: LocalDateTime,
    
    @Field("time_remaining")
    val timeRemaining: Long?
)

@Document
data class LocationInfoDocument(
    @Field("address")
    val address: String,
    
    @Field("city")
    val city: String,
    
    @Field("state")
    val state: String,
    
    @Field("zip_code")
    val zipCode: String,
    
    @Field("country")
    val country: String,
    
    @Field("coordinates")
    val coordinates: GeoCoordinates?,
    
    @Field("timezone")
    val timezone: String
)

@Document
data class LotMetadataDocument(
    @Field("tags")
    val tags: List<String>,
    
    @Field("categories")
    val categories: List<String>,
    
    @Field("seller_info")
    val sellerInfo: SellerInfoDocument,
    
    @Field("inspection")
    val inspection: InspectionInfoDocument?,
    
    @Field("history")
    val history: VehicleHistoryDocument?,
    
    @Field("fees")
    val fees: List<FeeDocument>,
    
    @Field("shipping")
    val shipping: ShippingInfoDocument?
)

@Document
data class SellerInfoDocument(
    @Field("id")
    val id: String,
    
    @Field("name")
    val name: String,
    
    @Field("type")
    val type: SellerType,
    
    @Field("rating")
    val rating: Double?,
    
    @Field("total_sales")
    val totalSales: Int?,
    
    @Field("response_rate")
    val responseRate: Double?
)

@Document
data class InspectionInfoDocument(
    @Field("inspected")
    val inspected: Boolean,
    
    @Field("inspection_date")
    val inspectionDate: LocalDateTime?,
    
    @Field("inspector")
    val inspector: String?,
    
    @Field("report_url")
    val reportUrl: String?,
    
    @Field("overall_condition")
    val overallCondition: String?,
    
    @Field("key_findings")
    val keyFindings: List<String>
)

@Document
data class VehicleHistoryDocument(
    @Field("accidents")
    val accidents: Int,
    
    @Field("owners")
    val owners: Int,
    
    @Field("title_status")
    val titleStatus: TitleStatus,
    
    @Field("service_records")
    val serviceRecords: Boolean,
    
    @Field("last_service_date")
    val lastServiceDate: LocalDateTime?
)

@Document
data class FeeDocument(
    @Field("type")
    val type: FeeType,
    
    @Field("amount")
    val amount: Double,
    
    @Field("description")
    val description: String,
    
    @Field("mandatory")
    val mandatory: Boolean
)

@Document
data class ShippingInfoDocument(
    @Field("available")
    val available: Boolean,
    
    @Field("estimated_cost")
    val estimatedCost: Double?,
    
    @Field("methods")
    val methods: List<ShippingMethod>,
    
    @Field("restrictions")
    val restrictions: List<String>
)
