package com.procar.auction.converter

import com.procar.auction.document.WarehouseDocument
import com.procar.provider.admin.AdminCreateWarehouseRequest
import com.procar.provider.admin.AdminWarehouseResponse
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class AdminCreateWarehouseRequestToDocumentConverter : Converter<AdminCreateWarehouseRequest, WarehouseDocument> {

    override fun convert(source: AdminCreateWarehouseRequest): WarehouseDocument {
        return WarehouseDocument(
            name = source.name,
            address = source.address,
            city = source.city,
            state = source.state,
            zipCode = source.zipCode,
            country = source.country,
            coordinates = source.coordinates,
            timezone = source.timezone,
            contactName = source.contactName,
            contactPhone = source.contactPhone,
            contactEmail = source.contactEmail
        )
    }
}

@Component
class WarehouseDocumentToAdminResponseConverter : Converter<WarehouseDocument, AdminWarehouseResponse> {

    override fun convert(source: WarehouseDocument): AdminWarehouseResponse {
        return AdminWarehouseResponse(
            id = source.id,
            name = source.name,
            address = source.address,
            city = source.city,
            state = source.state,
            zipCode = source.zipCode,
            country = source.country,
            coordinates = source.coordinates,
            timezone = source.timezone,
            contactName = source.contactName,
            contactPhone = source.contactPhone,
            contactEmail = source.contactEmail,
            createdAt = source.createdAt,
            updatedAt = source.updatedAt
        )
    }
}
