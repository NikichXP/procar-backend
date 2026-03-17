package com.procar.auction.converter

import com.procar.auction.document.*
import com.procar.provider.admin.AdminAuctionInfoRequest
import com.procar.provider.admin.AdminCreateLotRequest
import com.procar.provider.admin.AdminDamageInfoRequest
import com.procar.provider.admin.AdminDocumentRequest
import com.procar.provider.admin.AdminEngineInfoRequest
import com.procar.provider.admin.AdminFeeRequest
import com.procar.provider.admin.AdminInspectionInfoRequest
import com.procar.provider.admin.AdminLocationInfoRequest
import com.procar.provider.admin.AdminLotMetadataRequest
import com.procar.provider.admin.AdminSellerInfoRequest
import com.procar.provider.admin.AdminShippingInfoRequest
import com.procar.provider.admin.AdminUpdateLotRequest
import com.procar.provider.admin.AdminVehicleHistoryRequest
import com.procar.provider.admin.AdminVehicleImageRequest
import com.procar.provider.admin.AdminVehicleInfoRequest
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class AdminCreateLotRequestToLotDocumentConverter : Converter<AdminCreateLotRequest, LotDocument> {
    
    override fun convert(source: AdminCreateLotRequest): LotDocument {
        return LotDocument(
            externalId = source.externalId,
            title = source.title,
            description = source.description,
            vehicle = convertVehicleInfo(source.vehicle),
            auction = convertAuctionInfo(source.auction),
            location = convertLocationInfo(source.location),
            metadata = convertLotMetadata(source.metadata),
            status = source.status
        )
    }
    
    private fun convertVehicleInfo(vehicle: AdminVehicleInfoRequest): VehicleInfoDocument {
        return VehicleInfoDocument(
            vin = vehicle.vin,
            make = vehicle.make,
            model = vehicle.model,
            year = vehicle.year,
            trim = vehicle.trim,
            bodyType = vehicle.bodyType,
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
    }
    
    private fun convertEngineInfo(engine: AdminEngineInfoRequest): EngineInfoDocument {
        return EngineInfoDocument(
            type = engine.type,
            displacement = engine.displacement,
            cylinders = engine.cylinders,
            horsepower = engine.horsepower,
            torque = engine.torque
        )
    }
    
    private fun convertVehicleImage(image: AdminVehicleImageRequest): VehicleImageDocument {
        return VehicleImageDocument(
            url = image.url,
            type = image.type,
            description = image.description,
            isPrimary = image.isPrimary
        )
    }
    
    private fun convertDocument(doc: AdminDocumentRequest): DocumentDocument {
        return DocumentDocument(
            type = doc.type,
            url = doc.url,
            description = doc.description
        )
    }
    
    private fun convertDamageInfo(damage: AdminDamageInfoRequest): DamageInfoDocument {
        return DamageInfoDocument(
            area = damage.area,
            severity = damage.severity,
            description = damage.description,
            estimatedRepairCost = damage.estimatedRepairCost
        )
    }
    
    private fun convertAuctionInfo(auction: AdminAuctionInfoRequest): AuctionInfoDocument {
        return AuctionInfoDocument(
            currentBid = auction.currentBid,
            startingBid = auction.startingBid,
            reservePrice = auction.reservePrice,
            bidIncrement = auction.bidIncrement,
            totalBids = 0,
            startTime = auction.startTime,
            endTime = auction.endTime,
            timeRemaining = auction.timeRemaining,
            auctionType = auction.auctionType,
            buyItNowPrice = auction.buyItNowPrice
        )
    }
    
    private fun convertLocationInfo(location: AdminLocationInfoRequest): LocationInfoDocument {
        return LocationInfoDocument(
            address = location.address,
            city = location.city,
            state = location.state,
            zipCode = location.zipCode,
            country = location.country,
            coordinates = location.coordinates,
            timezone = location.timezone
        )
    }
    
    private fun convertLotMetadata(metadata: AdminLotMetadataRequest): LotMetadataDocument {
        return LotMetadataDocument(
            tags = metadata.tags,
            categories = metadata.categories,
            sellerInfo = convertSellerInfo(metadata.sellerInfo),
            inspection = metadata.inspection?.let { convertInspectionInfo(it) },
            history = metadata.history?.let { convertVehicleHistory(it) },
            fees = metadata.fees.map { convertFee(it) },
            shipping = metadata.shipping?.let { convertShippingInfo(it) }
        )
    }
    
    private fun convertSellerInfo(seller: AdminSellerInfoRequest): SellerInfoDocument {
        return SellerInfoDocument(
            id = seller.id,
            name = seller.name,
            type = seller.type,
            rating = seller.rating,
            totalSales = seller.totalSales,
            responseRate = seller.responseRate
        )
    }
    
    private fun convertInspectionInfo(inspection: AdminInspectionInfoRequest): InspectionInfoDocument {
        return InspectionInfoDocument(
            inspected = inspection.inspected,
            inspectionDate = inspection.inspectionDate,
            inspector = inspection.inspector,
            reportUrl = inspection.reportUrl,
            overallCondition = inspection.overallCondition,
            keyFindings = inspection.keyFindings
        )
    }
    
    private fun convertVehicleHistory(history: AdminVehicleHistoryRequest): VehicleHistoryDocument {
        return VehicleHistoryDocument(
            accidents = history.accidents,
            owners = history.owners,
            titleStatus = history.titleStatus,
            serviceRecords = history.serviceRecords,
            lastServiceDate = history.lastServiceDate
        )
    }
    
    private fun convertFee(fee: AdminFeeRequest): FeeDocument {
        return FeeDocument(
            type = fee.type,
            amount = fee.amount,
            description = fee.description,
            mandatory = fee.mandatory
        )
    }
    
    private fun convertShippingInfo(shipping: AdminShippingInfoRequest): ShippingInfoDocument {
        return ShippingInfoDocument(
            available = shipping.available,
            estimatedCost = shipping.estimatedCost,
            methods = shipping.methods,
            restrictions = shipping.restrictions
        )
    }
}

@Component
class AdminUpdateLotRequestToLotDocumentConverter : Converter<Pair<AdminUpdateLotRequest, LotDocument>, LotDocument> {
    
    override fun convert(source: Pair<AdminUpdateLotRequest, LotDocument>): LotDocument {
        val (request, existing) = source
        return existing.copy(
            title = request.title ?: existing.title,
            description = request.description ?: existing.description,
            vehicle = request.vehicle?.let { convertVehicleInfo(it) } ?: existing.vehicle,
            auction = request.auction?.let { convertAuctionInfo(it) } ?: existing.auction,
            location = request.location?.let { convertLocationInfo(it) } ?: existing.location,
            metadata = request.metadata?.let { convertLotMetadata(it) } ?: existing.metadata,
            status = request.status ?: existing.status,
            updatedAt = LocalDateTime.now()
        )
    }
    
    private fun convertVehicleInfo(vehicle: AdminVehicleInfoRequest): VehicleInfoDocument {
        return VehicleInfoDocument(
            vin = vehicle.vin,
            make = vehicle.make,
            model = vehicle.model,
            year = vehicle.year,
            trim = vehicle.trim,
            bodyType = vehicle.bodyType,
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
    }
    
    private fun convertEngineInfo(engine: AdminEngineInfoRequest): EngineInfoDocument {
        return EngineInfoDocument(
            type = engine.type,
            displacement = engine.displacement,
            cylinders = engine.cylinders,
            horsepower = engine.horsepower,
            torque = engine.torque
        )
    }
    
    private fun convertVehicleImage(image: AdminVehicleImageRequest): VehicleImageDocument {
        return VehicleImageDocument(
            url = image.url,
            type = image.type,
            description = image.description,
            isPrimary = image.isPrimary
        )
    }
    
    private fun convertDocument(doc: AdminDocumentRequest): DocumentDocument {
        return DocumentDocument(
            type = doc.type,
            url = doc.url,
            description = doc.description
        )
    }
    
    private fun convertDamageInfo(damage: AdminDamageInfoRequest): DamageInfoDocument {
        return DamageInfoDocument(
            area = damage.area,
            severity = damage.severity,
            description = damage.description,
            estimatedRepairCost = damage.estimatedRepairCost
        )
    }
    
    private fun convertAuctionInfo(auction: AdminAuctionInfoRequest): AuctionInfoDocument {
        return AuctionInfoDocument(
            currentBid = auction.currentBid,
            startingBid = auction.startingBid,
            reservePrice = auction.reservePrice,
            bidIncrement = auction.bidIncrement,
            totalBids = 0,
            startTime = auction.startTime,
            endTime = auction.endTime,
            timeRemaining = auction.timeRemaining,
            auctionType = auction.auctionType,
            buyItNowPrice = auction.buyItNowPrice
        )
    }
    
    private fun convertLocationInfo(location: AdminLocationInfoRequest): LocationInfoDocument {
        return LocationInfoDocument(
            address = location.address,
            city = location.city,
            state = location.state,
            zipCode = location.zipCode,
            country = location.country,
            coordinates = location.coordinates,
            timezone = location.timezone
        )
    }
    
    private fun convertLotMetadata(metadata: AdminLotMetadataRequest): LotMetadataDocument {
        return LotMetadataDocument(
            tags = metadata.tags,
            categories = metadata.categories,
            sellerInfo = convertSellerInfo(metadata.sellerInfo),
            inspection = metadata.inspection?.let { convertInspectionInfo(it) },
            history = metadata.history?.let { convertVehicleHistory(it) },
            fees = metadata.fees.map { convertFee(it) },
            shipping = metadata.shipping?.let { convertShippingInfo(it) }
        )
    }
    
    private fun convertSellerInfo(seller: AdminSellerInfoRequest): SellerInfoDocument {
        return SellerInfoDocument(
            id = seller.id,
            name = seller.name,
            type = seller.type,
            rating = seller.rating,
            totalSales = seller.totalSales,
            responseRate = seller.responseRate
        )
    }
    
    private fun convertInspectionInfo(inspection: AdminInspectionInfoRequest): InspectionInfoDocument {
        return InspectionInfoDocument(
            inspected = inspection.inspected,
            inspectionDate = inspection.inspectionDate,
            inspector = inspection.inspector,
            reportUrl = inspection.reportUrl,
            overallCondition = inspection.overallCondition,
            keyFindings = inspection.keyFindings
        )
    }
    
    private fun convertVehicleHistory(history: AdminVehicleHistoryRequest): VehicleHistoryDocument {
        return VehicleHistoryDocument(
            accidents = history.accidents,
            owners = history.owners,
            titleStatus = history.titleStatus,
            serviceRecords = history.serviceRecords,
            lastServiceDate = history.lastServiceDate
        )
    }
    
    private fun convertFee(fee: AdminFeeRequest): FeeDocument {
        return FeeDocument(
            type = fee.type,
            amount = fee.amount,
            description = fee.description,
            mandatory = fee.mandatory
        )
    }
    
    private fun convertShippingInfo(shipping: AdminShippingInfoRequest): ShippingInfoDocument {
        return ShippingInfoDocument(
            available = shipping.available,
            estimatedCost = shipping.estimatedCost,
            methods = shipping.methods,
            restrictions = shipping.restrictions
        )
    }
}
