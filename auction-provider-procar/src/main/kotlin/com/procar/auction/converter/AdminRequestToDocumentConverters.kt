package com.procar.auction.converter

import com.procar.auction.document.*
import com.procar.provider.admin.*
import com.procar.provider.lot.SellerType
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class AdminLotDocumentMapper {

    fun convertVehicleInfo(vehicle: AdminVehicleInfoRequest) = VehicleInfoDocument(
        vin = vehicle.vin,
        make = vehicle.make,
        model = vehicle.model,
        year = vehicle.year,
        trim = vehicle.trim,
        bodyType = vehicle.bodyType.name,
        color = vehicle.color,
        interiorColor = vehicle.interiorColor,
        mileage = vehicle.mileage,
        engine = convertEngineInfo(vehicle.engine),
        transmission = vehicle.transmission,
        drivetrain = vehicle.drivetrain,
        fuelType = vehicle.fuelType,
        condition = vehicle.condition,
        features = vehicle.features,
        images = vehicle.images.map { convertVehicleImage(it) },
        documents = vehicle.documents.map { convertDocument(it) },
        damage = vehicle.damage?.map { convertDamageInfo(it) }
    )

    fun convertAuctionInfo(auction: AdminAuctionInfoRequest) = AuctionInfoDocument(
        currentBid = auction.currentBid,
        startingBid = auction.startingBid,
        reservePrice = auction.reservePrice,
        bidIncrement = auction.bidIncrement,
        totalBids = 0,
        startTime = auction.startTime,
        endTime = auction.endTime,
        timeRemaining = auction.timeRemaining
    )

    fun convertLocationInfo(location: AdminLocationInfoRequest) = LocationInfoDocument(
        address = location.address,
        city = location.city,
        state = location.state,
        zipCode = location.zipCode,
        country = location.country,
        coordinates = location.coordinates,
        timezone = location.timezone
    )

    fun convertLotMetadata(metadata: AdminLotMetadataRequest) = LotMetadataDocument(
        tags = metadata.tags,
        categories = metadata.categories,
        sellerInfo = convertSellerInfo(metadata.sellerInfo),
        inspection = metadata.inspection?.let { convertInspectionInfo(it) },
        history = metadata.history?.let { convertVehicleHistory(it) },
        fees = metadata.fees.map { convertFee(it) },
        shipping = metadata.shipping?.let { convertShippingInfo(it) }
    )

    fun convertSellerInfo(seller: AdminSellerInfoRequest?): SellerInfoDocument {
        if (seller == null) return SellerInfoDocument(id = "", name = "", type = SellerType.DEALER, rating = null, totalSales = null, responseRate = null)
        return SellerInfoDocument(
            id = seller.id,
            name = seller.name,
            type = seller.type,
            rating = seller.rating,
            totalSales = seller.totalSales,
            responseRate = seller.responseRate
        )
    }

    private fun convertEngineInfo(engine: AdminEngineInfoRequest) = EngineInfoDocument(
        type = engine.type,
        displacement = engine.displacement,
        cylinders = engine.cylinders,
        horsepower = engine.horsepower,
        torque = engine.torque
    )

    private fun convertVehicleImage(image: AdminVehicleImageRequest) = VehicleImageDocument(
        url = image.url,
        type = image.type,
        description = image.description,
        isPrimary = image.isPrimary
    )

    private fun convertDocument(doc: AdminDocumentRequest) = DocumentDocument(
        type = doc.type,
        url = doc.url,
        description = doc.description
    )

    private fun convertDamageInfo(damage: AdminDamageInfoRequest) = DamageInfoDocument(
        area = damage.area,
        severity = damage.severity,
        description = damage.description,
        estimatedRepairCost = damage.estimatedRepairCost
    )

    private fun convertInspectionInfo(inspection: AdminInspectionInfoRequest) = InspectionInfoDocument(
        inspected = inspection.inspected,
        inspectionDate = inspection.inspectionDate,
        inspector = inspection.inspector,
        reportUrl = inspection.reportUrl,
        overallCondition = inspection.overallCondition,
        keyFindings = inspection.keyFindings
    )

    private fun convertVehicleHistory(history: AdminVehicleHistoryRequest) = VehicleHistoryDocument(
        accidents = history.accidents,
        owners = history.owners,
        titleStatus = history.titleStatus,
        serviceRecords = history.serviceRecords,
        lastServiceDate = history.lastServiceDate
    )

    private fun convertFee(fee: AdminFeeRequest) = FeeDocument(
        type = fee.type,
        amount = fee.amount,
        description = fee.description,
        mandatory = fee.mandatory
    )

    private fun convertShippingInfo(shipping: AdminShippingInfoRequest) = ShippingInfoDocument(
        available = shipping.available,
        estimatedCost = shipping.estimatedCost,
        methods = shipping.methods,
        restrictions = shipping.restrictions
    )
}

@Component
class AdminCreateLotRequestToLotDocumentConverter(
    private val mapper: AdminLotDocumentMapper
) : Converter<AdminCreateLotRequest, LotEntity> {

    override fun convert(source: AdminCreateLotRequest) = LotEntity(
        externalId = source.externalId,
        title = source.title,
        description = source.description,
        vehicle = mapper.convertVehicleInfo(source.vehicle),
        auction = source.auction?.let(mapper::convertAuctionInfo),
        location = mapper.convertLocationInfo(source.location),
        metadata = mapper.convertLotMetadata(source.metadata),
        status = source.status,
        brokerOrgId = source.brokerOrgId,
        lotType = source.lotType,
        buyoutPrice = source.buyoutPrice,
        brand = source.brand,
    )
}

@Component
class AdminUpdateLotRequestToLotDocumentConverter(
    private val mapper: AdminLotDocumentMapper
) : Converter<Pair<AdminUpdateLotRequest, LotEntity>, LotEntity> {

    override fun convert(source: Pair<AdminUpdateLotRequest, LotEntity>): LotEntity {
        val (request, existing) = source
        return existing.copy(
            title = request.title ?: existing.title,
            description = request.description ?: existing.description,
            vehicle = request.vehicle?.let(mapper::convertVehicleInfo) ?: existing.vehicle,
            auction = request.auction?.let(mapper::convertAuctionInfo) ?: existing.auction,
            location = request.location?.let(mapper::convertLocationInfo) ?: existing.location,
            metadata = request.metadata?.let(mapper::convertLotMetadata) ?: existing.metadata,
            status = request.status ?: existing.status,
            brokerOrgId = request.brokerOrgId,
            lotType = request.lotType ?: existing.lotType,
            buyoutPrice = request.buyoutPrice ?: existing.buyoutPrice,
            brand = request.brand ?: existing.brand,
            updatedAt = LocalDateTime.now()
        )
    }
}
