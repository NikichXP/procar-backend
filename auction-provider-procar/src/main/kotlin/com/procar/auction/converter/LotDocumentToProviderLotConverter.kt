package com.procar.auction.converter

import com.procar.auction.document.LotDocument
import com.procar.provider.lot.AuctionInfo
import com.procar.provider.lot.DamageInfo
import com.procar.provider.lot.Document
import com.procar.provider.lot.EngineInfo
import com.procar.provider.lot.Fee
import com.procar.provider.lot.InspectionInfo
import com.procar.provider.lot.LocationInfo
import com.procar.provider.lot.LotMetadata
import com.procar.provider.lot.VehicleLot
import com.procar.provider.lot.SellerInfo
import com.procar.provider.lot.ShippingInfo
import com.procar.provider.lot.VehicleHistory
import com.procar.provider.lot.VehicleImage
import com.procar.provider.lot.VehicleInfo
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class LotDocumentToProviderLotConverter : Converter<LotDocument, VehicleLot> {

    override fun convert(source: LotDocument): VehicleLot {
        return VehicleLot(
            id = source.id!!,
            providerId = source.providerId,
            providerName = source.providerName,
            externalId = source.externalId,
            title = source.title,
            description = source.description,
            vehicle = convertVehicleInfo(source.vehicle),
            auction = convertAuctionInfo(source.auction),
            location = convertLocationInfo(source.location),
            metadata = convertLotMetadata(source.metadata),
            status = source.status,
            createdAt = source.createdAt,
            updatedAt = source.updatedAt
        )
    }

    private fun convertVehicleInfo(vehicle: com.procar.auction.document.VehicleInfoDocument): VehicleInfo {
        return VehicleInfo(
            vin = vehicle.vin,
            make = vehicle.make,
            model = vehicle.model,
            year = vehicle.year,
            trim = vehicle.trim,
            bodyType = vehicle.bodyType,
            color = vehicle.color,
            interiorColor = vehicle.interiorColor,
            mileage = vehicle.mileage,
            engine = EngineInfo(
                type = vehicle.engine.type,
                displacement = vehicle.engine.displacement,
                cylinders = vehicle.engine.cylinders,
                horsepower = vehicle.engine.horsepower,
                torque = vehicle.engine.torque
            ),
            transmission = vehicle.transmission,
            drivetrain = vehicle.drivetrain,
            fuelType = vehicle.fuelType,
            condition = vehicle.condition,
            features = vehicle.features,
            images = vehicle.images.map { 
                VehicleImage(
                    url = it.url,
                    type = it.type,
                    description = it.description,
                    isPrimary = it.isPrimary
                )
            },
            documents = vehicle.documents.map {
                Document(
                    type = it.type,
                    url = it.url,
                    description = it.description
                )
            },
            damage = vehicle.damage?.map {
                DamageInfo(
                    area = it.area,
                    severity = it.severity,
                    description = it.description,
                    estimatedRepairCost = it.estimatedRepairCost
                )
            }
        )
    }

    private fun convertAuctionInfo(auction: com.procar.auction.document.AuctionInfoDocument): AuctionInfo {
        return AuctionInfo(
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

    private fun convertLocationInfo(location: com.procar.auction.document.LocationInfoDocument): LocationInfo {
        return LocationInfo(
            address = location.address,
            city = location.city,
            state = location.state,
            zipCode = location.zipCode,
            country = location.country,
            coordinates = location.coordinates,
            timezone = location.timezone
        )
    }

    private fun convertLotMetadata(metadata: com.procar.auction.document.LotMetadataDocument): LotMetadata {
        return LotMetadata(
            tags = metadata.tags,
            categories = metadata.categories,
            sellerInfo = SellerInfo(
                id = metadata.sellerInfo.id,
                name = metadata.sellerInfo.name,
                type = metadata.sellerInfo.type,
                rating = metadata.sellerInfo.rating,
                totalSales = metadata.sellerInfo.totalSales,
                responseRate = metadata.sellerInfo.responseRate
            ),
            inspection = metadata.inspection?.let {
                InspectionInfo(
                    inspected = it.inspected,
                    inspectionDate = it.inspectionDate,
                    inspector = it.inspector,
                    reportUrl = it.reportUrl,
                    overallCondition = it.overallCondition,
                    keyFindings = it.keyFindings
                )
            },
            history = metadata.history?.let {
                VehicleHistory(
                    accidents = it.accidents,
                    owners = it.owners,
                    titleStatus = it.titleStatus,
                    serviceRecords = it.serviceRecords,
                    lastServiceDate = it.lastServiceDate
                )
            },
            fees = metadata.fees.map {
                Fee(
                    type = it.type,
                    amount = it.amount,
                    description = it.description,
                    mandatory = it.mandatory
                )
            },
            shipping = metadata.shipping?.let {
                ShippingInfo(
                    available = it.available,
                    estimatedCost = it.estimatedCost,
                    methods = it.methods,
                    restrictions = it.restrictions
                )
            }
        )
    }
}
