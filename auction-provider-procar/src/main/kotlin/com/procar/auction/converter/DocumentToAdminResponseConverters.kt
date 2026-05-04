package com.procar.auction.converter

import com.procar.auction.document.*
import com.procar.provider.admin.*
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class LotDocumentToAdminLotResponseConverter : Converter<LotEntity, AdminLotResponse> {
    
    override fun convert(source: LotEntity): AdminLotResponse {
        return AdminLotResponse(
            id = source.id,
            externalId = source.externalId,
            title = source.title,
            description = source.description,
            vehicle = convertVehicleInfo(source.vehicle),
            auction = source.auction?.let(::convertAuctionInfo),
            location = convertLocationInfo(source.location),
            metadata = convertLotMetadata(source.metadata),
            status = source.status,
            brokerOrgId = source.brokerOrgId,
            createdAt = source.createdAt,
            updatedAt = source.updatedAt,
            lotType = source.lotType,
            buyoutPrice = source.buyoutPrice,
        )
    }
    
    private fun convertVehicleInfo(vehicle: VehicleInfoDocument): AdminVehicleInfoResponse {
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
    
    private fun convertEngineInfo(engine: EngineInfoDocument): AdminEngineInfoResponse {
        return AdminEngineInfoResponse(
            type = engine.type,
            displacement = engine.displacement,
            cylinders = engine.cylinders,
            horsepower = engine.horsepower,
            torque = engine.torque
        )
    }
    
    private fun convertVehicleImage(image: VehicleImageDocument): AdminVehicleImageResponse {
        return AdminVehicleImageResponse(
            url = image.url,
            type = image.type,
            description = image.description,
            isPrimary = image.isPrimary
        )
    }
    
    private fun convertDocument(doc: DocumentDocument): AdminDocumentResponse {
        return AdminDocumentResponse(
            type = doc.type,
            url = doc.url,
            description = doc.description
        )
    }
    
    private fun convertDamageInfo(damage: DamageInfoDocument): AdminDamageInfoResponse {
        return AdminDamageInfoResponse(
            area = damage.area,
            severity = damage.severity,
            description = damage.description,
            estimatedRepairCost = damage.estimatedRepairCost
        )
    }
    
    private fun convertAuctionInfo(auction: AuctionInfoDocument): AdminAuctionInfoResponse {
        return AdminAuctionInfoResponse(
            currentBid = auction.currentBid,
            startingBid = auction.startingBid,
            reservePrice = auction.reservePrice,
            bidIncrement = auction.bidIncrement,
            totalBids = auction.totalBids,
            startTime = auction.startTime,
            endTime = auction.endTime,
            timeRemaining = auction.timeRemaining
        )
    }
    
    private fun convertLocationInfo(location: LocationInfoDocument): AdminLocationInfoResponse {
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
    
    private fun convertLotMetadata(metadata: LotMetadataDocument): AdminLotMetadataResponse {
        return AdminLotMetadataResponse(
            tags = metadata.tags,
            categories = metadata.categories,
            sellerInfo = convertSellerInfo(metadata.sellerInfo),
            inspection = metadata.inspection?.let { convertInspectionInfo(it) },
            history = metadata.history?.let { convertVehicleHistory(it) },
            fees = metadata.fees.map { convertFee(it) },
            shipping = metadata.shipping?.let { convertShippingInfo(it) }
        )
    }
    
    private fun convertSellerInfo(seller: SellerInfoDocument): AdminSellerInfoResponse {
        return AdminSellerInfoResponse(
            id = seller.id,
            name = seller.name,
            type = seller.type,
            rating = seller.rating,
            totalSales = seller.totalSales,
            responseRate = seller.responseRate
        )
    }
    
    private fun convertInspectionInfo(inspection: InspectionInfoDocument): AdminInspectionInfoResponse {
        return AdminInspectionInfoResponse(
            inspected = inspection.inspected,
            inspectionDate = inspection.inspectionDate,
            inspector = inspection.inspector,
            reportUrl = inspection.reportUrl,
            overallCondition = inspection.overallCondition,
            keyFindings = inspection.keyFindings
        )
    }
    
    private fun convertVehicleHistory(history: VehicleHistoryDocument): AdminVehicleHistoryResponse {
        return AdminVehicleHistoryResponse(
            accidents = history.accidents,
            owners = history.owners,
            titleStatus = history.titleStatus,
            serviceRecords = history.serviceRecords,
            lastServiceDate = history.lastServiceDate
        )
    }
    
    private fun convertFee(fee: FeeDocument): AdminFeeResponse {
        return AdminFeeResponse(
            type = fee.type,
            amount = fee.amount,
            description = fee.description,
            mandatory = fee.mandatory
        )
    }
    
    private fun convertShippingInfo(shipping: ShippingInfoDocument): AdminShippingInfoResponse {
        return AdminShippingInfoResponse(
            available = shipping.available,
            estimatedCost = shipping.estimatedCost,
            methods = shipping.methods,
            restrictions = shipping.restrictions
        )
    }
}
