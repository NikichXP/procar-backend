package com.procar.auction.api.admin

import com.procar.auction.document.*
import com.procar.provider.common.GeoCoordinates
import com.procar.provider.common.PaginationResponse
import com.procar.provider.lot.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

data class AdminCreateLotRequest(
    val externalId: String,

    val title: String,

    val description: String,

    @field:Valid
    val vehicle: AdminVehicleInfoRequest,

    @field:Valid
    val auction: AdminAuctionInfoRequest,

    @field:Valid
    val location: AdminLocationInfoRequest,

    @field:Valid
    val metadata: AdminLotMetadataRequest,

    val status: LotStatus
) {
    fun toLotDocument(): LotDocument {
        return LotDocument(
            externalId = externalId,
            title = title,
            description = description,
            vehicle = vehicle.toVehicleInfoDocument(),
            auction = auction.toAuctionInfoDocument(),
            location = location.toLocationInfoDocument(),
            metadata = metadata.toLotMetadataDocument(),
            status = status
        )
    }
}

data class AdminUpdateLotRequest(
    val title: String? = null,
    val description: String? = null,
    val vehicle: AdminVehicleInfoRequest? = null,
    val auction: AdminAuctionInfoRequest? = null,
    val location: AdminLocationInfoRequest? = null,
    val metadata: AdminLotMetadataRequest? = null,
    val status: LotStatus? = null
) {
    fun toLotDocument(existing: LotDocument): LotDocument {
        return existing.copy(
            title = title ?: existing.title,
            description = description ?: existing.description,
            vehicle = vehicle?.toVehicleInfoDocument() ?: existing.vehicle,
            auction = auction?.toAuctionInfoDocument() ?: existing.auction,
            location = location?.toLocationInfoDocument() ?: existing.location,
            metadata = metadata?.toLotMetadataDocument() ?: existing.metadata,
            status = status ?: existing.status,
            updatedAt = LocalDateTime.now()
        )
    }
}

data class AdminUpdateStatusRequest(
    val status: LotStatus
)

data class AdminVehicleInfoRequest(
    val vin: String? = null,
    val make: String,
    val model: String,
    @field:Positive val year: Int,
    val trim: String? = null,
    val bodyType: String,
    val color: String? = null,
    val interiorColor: String? = null,
    val mileage: Int? = null,

    @field:Valid
    val engine: AdminEngineInfoRequest,

    val transmission: TransmissionType,

    val drivetrain: DrivetrainType,

    val fuelType: FuelType,

    val condition: VehicleCondition,

    val features: List<String> = emptyList(),
    val images: List<AdminVehicleImageRequest> = emptyList(),
    val documents: List<AdminDocumentRequest> = emptyList(),
    val damage: List<AdminDamageInfoRequest>? = null
) {
    fun toVehicleInfoDocument(): VehicleInfoDocument {
        return VehicleInfoDocument(
            vin = vin,
            make = make,
            model = model,
            year = year,
            trim = trim,
            bodyType = bodyType,
            color = color,
            interiorColor = interiorColor,
            mileage = mileage,
            engine = engine.toEngineInfoDocument(),
            transmission = transmission,
            drivetrain = drivetrain,
            fuelType = fuelType,
            condition = condition,
            features = features,
            images = images.map { it.toVehicleImageDocument() },
            documents = documents.map { it.toDocumentDocument() },
            damage = damage?.map { it.toDamageInfoDocument() }
        )
    }
}

data class AdminEngineInfoRequest(
    val type: String,
    val displacement: Double? = null,
    val cylinders: Int? = null,
    val horsepower: Int? = null,
    val torque: Int? = null
) {
    fun toEngineInfoDocument(): EngineInfoDocument {
        return EngineInfoDocument(
            type = type,
            displacement = displacement,
            cylinders = cylinders,
            horsepower = horsepower,
            torque = torque
        )
    }
}

data class AdminVehicleImageRequest(
    val url: String,
    val type: ImageType,
    val description: String? = null,
    val isPrimary: Boolean = false
) {
    fun toVehicleImageDocument(): VehicleImageDocument {
        return VehicleImageDocument(
            url = url,
            type = type,
            description = description,
            isPrimary = isPrimary
        )
    }
}

data class AdminDocumentRequest(
    val type: DocumentType,
    val url: String,
    val description: String? = null
) {
    fun toDocumentDocument(): DocumentDocument {
        return DocumentDocument(
            type = type,
            url = url,
            description = description
        )
    }
}

data class AdminDamageInfoRequest(
    val area: String,
    val severity: DamageSeverity,
    val description: String,
    val estimatedRepairCost: Double? = null
) {
    fun toDamageInfoDocument(): DamageInfoDocument {
        return DamageInfoDocument(
            area = area,
            severity = severity,
            description = description,
            estimatedRepairCost = estimatedRepairCost
        )
    }
}

data class AdminAuctionInfoRequest(
    @field:Positive val currentBid: Double,
    @field:Positive val startingBid: Double,
    val reservePrice: Double? = null,
    @field:Positive val bidIncrement: Double,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val timeRemaining: Long? = null,
    val auctionType: AuctionType,
    val buyItNowPrice: Double? = null
) {
    fun toAuctionInfoDocument(): AuctionInfoDocument {
        return AuctionInfoDocument(
            currentBid = currentBid,
            startingBid = startingBid,
            reservePrice = reservePrice,
            bidIncrement = bidIncrement,
            totalBids = 0,
            startTime = startTime,
            endTime = endTime,
            timeRemaining = timeRemaining,
            auctionType = auctionType,
            buyItNowPrice = buyItNowPrice
        )
    }
}

data class AdminLocationInfoRequest(
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val coordinates: GeoCoordinates? = null,
    val timezone: String
) {
    fun toLocationInfoDocument(): LocationInfoDocument {
        return LocationInfoDocument(
            address = address,
            city = city,
            state = state,
            zipCode = zipCode,
            country = country,
            coordinates = coordinates,
            timezone = timezone
        )
    }
}

data class AdminLotMetadataRequest(
    val tags: List<String> = emptyList(),
    val categories: List<String> = emptyList(),

    @field:Valid
    val sellerInfo: AdminSellerInfoRequest,

    val inspection: AdminInspectionInfoRequest? = null,
    val history: AdminVehicleHistoryRequest? = null,
    val fees: List<AdminFeeRequest> = emptyList(),
    val shipping: AdminShippingInfoRequest? = null
) {
    fun toLotMetadataDocument(): LotMetadataDocument {
        return LotMetadataDocument(
            tags = tags,
            categories = categories,
            sellerInfo = sellerInfo.toSellerInfoDocument(),
            inspection = inspection?.toInspectionInfoDocument(),
            history = history?.toVehicleHistoryDocument(),
            fees = fees.map { it.toFeeDocument() },
            shipping = shipping?.toShippingInfoDocument()
        )
    }
}

data class AdminSellerInfoRequest(
    val id: String,
    val name: String,
    val type: SellerType,
    val rating: Double? = null,
    val totalSales: Int? = null,
    val responseRate: Double? = null
) {
    fun toSellerInfoDocument(): SellerInfoDocument {
        return SellerInfoDocument(
            id = id,
            name = name,
            type = type,
            rating = rating,
            totalSales = totalSales,
            responseRate = responseRate
        )
    }
}

data class AdminInspectionInfoRequest(
    val inspected: Boolean = false,
    val inspectionDate: LocalDateTime? = null,
    val inspector: String? = null,
    val reportUrl: String? = null,
    val overallCondition: String? = null,
    val keyFindings: List<String> = emptyList()
) {
    fun toInspectionInfoDocument(): InspectionInfoDocument {
        return InspectionInfoDocument(
            inspected = inspected,
            inspectionDate = inspectionDate,
            inspector = inspector,
            reportUrl = reportUrl,
            overallCondition = overallCondition,
            keyFindings = keyFindings
        )
    }
}

data class AdminVehicleHistoryRequest(
    val accidents: Int = 0,
    val owners: Int = 1,
    val titleStatus: TitleStatus,
    val serviceRecords: Boolean = false,
    val lastServiceDate: LocalDateTime? = null
) {
    fun toVehicleHistoryDocument(): VehicleHistoryDocument {
        return VehicleHistoryDocument(
            accidents = accidents,
            owners = owners,
            titleStatus = titleStatus,
            serviceRecords = serviceRecords,
            lastServiceDate = lastServiceDate
        )
    }
}

data class AdminFeeRequest(
    val type: FeeType,
    @field:Positive val amount: Double,
    val description: String,
    val mandatory: Boolean = false
) {
    fun toFeeDocument(): FeeDocument {
        return FeeDocument(
            type = type,
            amount = amount,
            description = description,
            mandatory = mandatory
        )
    }
}

data class AdminShippingInfoRequest(
    val available: Boolean = false,
    val estimatedCost: Double? = null,
    val methods: List<ShippingMethod> = emptyList(),
    val restrictions: List<String> = emptyList()
) {
    fun toShippingInfoDocument(): ShippingInfoDocument {
        return ShippingInfoDocument(
            available = available,
            estimatedCost = estimatedCost,
            methods = methods,
            restrictions = restrictions
        )
    }
}

data class AdminLotResponse(
    val id: String,
    val externalId: String,
    val title: String,
    val description: String,
    val vehicle: AdminVehicleInfoResponse,
    val auction: AdminAuctionInfoResponse,
    val location: AdminLocationInfoResponse,
    val metadata: AdminLotMetadataResponse,
    val status: LotStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun fromLotDocument(lot: LotDocument): AdminLotResponse {
            return AdminLotResponse(
                id = lot.id!!,
                externalId = lot.externalId,
                title = lot.title,
                description = lot.description,
                vehicle = AdminVehicleInfoResponse.fromVehicleInfoDocument(lot.vehicle),
                auction = AdminAuctionInfoResponse.fromAuctionInfoDocument(lot.auction),
                location = AdminLocationInfoResponse.fromLocationInfoDocument(lot.location),
                metadata = AdminLotMetadataResponse.fromLotMetadataDocument(lot.metadata),
                status = lot.status,
                createdAt = lot.createdAt,
                updatedAt = lot.updatedAt
            )
        }
    }
}

data class AdminVehicleInfoResponse(
    val vin: String?,
    val make: String,
    val model: String,
    val year: Int,
    val trim: String?,
    val bodyType: String,
    val color: String?,
    val interiorColor: String?,
    val mileage: Int?,
    val engine: AdminEngineInfoResponse,
    val transmission: TransmissionType,
    val drivetrain: DrivetrainType,
    val fuelType: FuelType,
    val condition: VehicleCondition,
    val features: List<String>,
    val images: List<AdminVehicleImageResponse>,
    val documents: List<AdminDocumentResponse>,
    val damage: List<AdminDamageInfoResponse>?
) {
    companion object {
        fun fromVehicleInfoDocument(vehicle: VehicleInfoDocument): AdminVehicleInfoResponse {
            return AdminVehicleInfoResponse(
                vin = vehicle.vin,
                make = vehicle.make,
                model = vehicle.model,
                year = vehicle.year,
                trim = vehicle.trim,
                bodyType = vehicle.bodyType,
                color = vehicle.color,
                interiorColor = vehicle.interiorColor,
                mileage = vehicle.mileage,
                engine = AdminEngineInfoResponse.fromEngineInfoDocument(vehicle.engine),
                transmission = vehicle.transmission,
                drivetrain = vehicle.drivetrain,
                fuelType = vehicle.fuelType,
                condition = vehicle.condition,
                features = vehicle.features,
                images = vehicle.images.map { AdminVehicleImageResponse.fromVehicleImageDocument(it) },
                documents = vehicle.documents.map { AdminDocumentResponse.fromDocumentDocument(it) },
                damage = vehicle.damage?.map { AdminDamageInfoResponse.fromDamageInfoDocument(it) }
            )
        }
    }
}

data class AdminEngineInfoResponse(
    val type: String,
    val displacement: Double?,
    val cylinders: Int?,
    val horsepower: Int?,
    val torque: Int?
) {
    companion object {
        fun fromEngineInfoDocument(engine: EngineInfoDocument): AdminEngineInfoResponse {
            return AdminEngineInfoResponse(
                type = engine.type,
                displacement = engine.displacement,
                cylinders = engine.cylinders,
                horsepower = engine.horsepower,
                torque = engine.torque
            )
        }
    }
}

data class AdminVehicleImageResponse(
    val url: String,
    val type: ImageType,
    val description: String?,
    val isPrimary: Boolean
) {
    companion object {
        fun fromVehicleImageDocument(image: VehicleImageDocument): AdminVehicleImageResponse {
            return AdminVehicleImageResponse(
                url = image.url,
                type = image.type,
                description = image.description,
                isPrimary = image.isPrimary
            )
        }
    }
}

data class AdminDocumentResponse(
    val type: DocumentType,
    val url: String,
    val description: String?
) {
    companion object {
        fun fromDocumentDocument(doc: DocumentDocument): AdminDocumentResponse {
            return AdminDocumentResponse(
                type = doc.type,
                url = doc.url,
                description = doc.description
            )
        }
    }
}

data class AdminDamageInfoResponse(
    val area: String,
    val severity: DamageSeverity,
    val description: String,
    val estimatedRepairCost: Double?
) {
    companion object {
        fun fromDamageInfoDocument(damage: DamageInfoDocument): AdminDamageInfoResponse {
            return AdminDamageInfoResponse(
                area = damage.area,
                severity = damage.severity,
                description = damage.description,
                estimatedRepairCost = damage.estimatedRepairCost
            )
        }
    }
}

data class AdminAuctionInfoResponse(
    val currentBid: Double,
    val startingBid: Double,
    val reservePrice: Double?,
    val bidIncrement: Double,
    val totalBids: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val timeRemaining: Long?,
    val auctionType: AuctionType,
    val buyItNowPrice: Double?
) {
    companion object {
        fun fromAuctionInfoDocument(auction: AuctionInfoDocument): AdminAuctionInfoResponse {
            return AdminAuctionInfoResponse(
                currentBid = auction.currentBid,
                startingBid = auction.startingBid,
                reservePrice = auction.reservePrice,
                bidIncrement = auction.bidIncrement,
                totalBids = auction.totalBids,
                startTime = auction.startTime,
                endTime = auction.endTime,
                timeRemaining = auction.timeRemaining,
                auctionType = auction.auctionType,
                buyItNowPrice = auction.buyItNowPrice
            )
        }
    }
}

data class AdminLocationInfoResponse(
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val coordinates: GeoCoordinates?,
    val timezone: String
) {
    companion object {
        fun fromLocationInfoDocument(location: LocationInfoDocument): AdminLocationInfoResponse {
            return AdminLocationInfoResponse(
                address = location.address,
                city = location.city,
                state = location.state,
                zipCode = location.zipCode,
                country = location.country,
                coordinates = location.coordinates,
                timezone = location.timezone
            )
        }
    }
}

data class AdminLotMetadataResponse(
    val tags: List<String>,
    val categories: List<String>,
    val sellerInfo: AdminSellerInfoResponse,
    val inspection: AdminInspectionInfoResponse?,
    val history: AdminVehicleHistoryResponse?,
    val fees: List<AdminFeeResponse>,
    val shipping: AdminShippingInfoResponse?
) {
    companion object {
        fun fromLotMetadataDocument(metadata: LotMetadataDocument): AdminLotMetadataResponse {
            return AdminLotMetadataResponse(
                tags = metadata.tags,
                categories = metadata.categories,
                sellerInfo = AdminSellerInfoResponse.fromSellerInfoDocument(metadata.sellerInfo),
                inspection = metadata.inspection?.let { AdminInspectionInfoResponse.fromInspectionInfoDocument(it) },
                history = metadata.history?.let { AdminVehicleHistoryResponse.fromVehicleHistoryDocument(it) },
                fees = metadata.fees.map { AdminFeeResponse.fromFeeDocument(it) },
                shipping = metadata.shipping?.let { AdminShippingInfoResponse.fromShippingInfoDocument(it) }
            )
        }
    }
}

data class AdminSellerInfoResponse(
    val id: String,
    val name: String,
    val type: SellerType,
    val rating: Double?,
    val totalSales: Int?,
    val responseRate: Double?
) {
    companion object {
        fun fromSellerInfoDocument(seller: SellerInfoDocument): AdminSellerInfoResponse {
            return AdminSellerInfoResponse(
                id = seller.id,
                name = seller.name,
                type = seller.type,
                rating = seller.rating,
                totalSales = seller.totalSales,
                responseRate = seller.responseRate
            )
        }
    }
}

data class AdminInspectionInfoResponse(
    val inspected: Boolean,
    val inspectionDate: LocalDateTime?,
    val inspector: String?,
    val reportUrl: String?,
    val overallCondition: String?,
    val keyFindings: List<String>
) {
    companion object {
        fun fromInspectionInfoDocument(inspection: InspectionInfoDocument): AdminInspectionInfoResponse {
            return AdminInspectionInfoResponse(
                inspected = inspection.inspected,
                inspectionDate = inspection.inspectionDate,
                inspector = inspection.inspector,
                reportUrl = inspection.reportUrl,
                overallCondition = inspection.overallCondition,
                keyFindings = inspection.keyFindings
            )
        }
    }
}

data class AdminVehicleHistoryResponse(
    val accidents: Int,
    val owners: Int,
    val titleStatus: TitleStatus,
    val serviceRecords: Boolean,
    val lastServiceDate: LocalDateTime?
) {
    companion object {
        fun fromVehicleHistoryDocument(history: VehicleHistoryDocument): AdminVehicleHistoryResponse {
            return AdminVehicleHistoryResponse(
                accidents = history.accidents,
                owners = history.owners,
                titleStatus = history.titleStatus,
                serviceRecords = history.serviceRecords,
                lastServiceDate = history.lastServiceDate
            )
        }
    }
}

data class AdminFeeResponse(
    val type: FeeType,
    val amount: Double,
    val description: String,
    val mandatory: Boolean
) {
    companion object {
        fun fromFeeDocument(fee: FeeDocument): AdminFeeResponse {
            return AdminFeeResponse(
                type = fee.type,
                amount = fee.amount,
                description = fee.description,
                mandatory = fee.mandatory
            )
        }
    }
}

data class AdminShippingInfoResponse(
    val available: Boolean,
    val estimatedCost: Double?,
    val methods: List<ShippingMethod>,
    val restrictions: List<String>
) {
    companion object {
        fun fromShippingInfoDocument(shipping: ShippingInfoDocument): AdminShippingInfoResponse {
            return AdminShippingInfoResponse(
                available = shipping.available,
                estimatedCost = shipping.estimatedCost,
                methods = shipping.methods,
                restrictions = shipping.restrictions
            )
        }
    }
}

data class AdminPaginatedLotsResponse(
    val lots: List<AdminLotResponse>,
    val pagination: PaginationResponse
)
