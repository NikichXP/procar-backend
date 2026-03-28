package com.procar.auction.api.admin

import com.procar.auction.document.WarehouseDocument
import com.procar.auction.service.WarehouseService
import com.procar.provider.admin.AdminCreateWarehouseRequest
import com.procar.provider.admin.AdminUpdateWarehouseRequest
import com.procar.provider.admin.AdminWarehouseController
import com.procar.provider.admin.AdminWarehouseResponse
import com.procar.provider.common.ApiResponse
import org.springframework.core.convert.ConversionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminWarehouseAPI(
    private val warehouseService: WarehouseService,
    private val conversionService: ConversionService
) : AdminWarehouseController {

    override suspend fun getAllWarehouses(): ResponseEntity<ApiResponse<List<AdminWarehouseResponse>>> {
        val warehouses = warehouseService.getAllWarehouses()
            .map { conversionService.convert(it, AdminWarehouseResponse::class.java)!! }
        return ResponseEntity.ok(ApiResponse(warehouses))
    }

    override suspend fun getWarehouse(warehouseId: String): ResponseEntity<ApiResponse<AdminWarehouseResponse>> {
        val warehouse = warehouseService.getWarehouseById(warehouseId)
            ?: return ResponseEntity.notFound().build()
        val response = conversionService.convert(warehouse, AdminWarehouseResponse::class.java)!!
        return ResponseEntity.ok(ApiResponse(response))
    }

    override suspend fun createWarehouse(request: AdminCreateWarehouseRequest): ResponseEntity<ApiResponse<AdminWarehouseResponse>> {
        val document = conversionService.convert(request, WarehouseDocument::class.java)!!
        val saved = warehouseService.createWarehouse(document)
        val response = conversionService.convert(saved, AdminWarehouseResponse::class.java)!!
        return ResponseEntity.ok(ApiResponse(response, "Warehouse created successfully"))
    }

    override suspend fun updateWarehouse(
        warehouseId: String,
        request: AdminUpdateWarehouseRequest
    ): ResponseEntity<ApiResponse<AdminWarehouseResponse>> {
        val existing = warehouseService.getWarehouseById(warehouseId)
            ?: return ResponseEntity.notFound().build()
        val updated = existing.copy(
            name = request.name ?: existing.name,
            address = request.address ?: existing.address,
            city = request.city ?: existing.city,
            state = request.state ?: existing.state,
            zipCode = request.zipCode ?: existing.zipCode,
            country = request.country ?: existing.country,
            coordinates = request.coordinates ?: existing.coordinates,
            timezone = request.timezone ?: existing.timezone,
            contactName = request.contactName ?: existing.contactName,
            contactPhone = request.contactPhone ?: existing.contactPhone,
            contactEmail = request.contactEmail ?: existing.contactEmail
        )
        val saved = warehouseService.updateWarehouse(warehouseId, updated)
            ?: return ResponseEntity.notFound().build()
        val response = conversionService.convert(saved, AdminWarehouseResponse::class.java)!!
        return ResponseEntity.ok(ApiResponse(response, "Warehouse updated successfully"))
    }

    override suspend fun deleteWarehouse(warehouseId: String): ResponseEntity<ApiResponse<Void?>> {
        val success = warehouseService.deleteWarehouse(warehouseId)
        return if (success) {
            ResponseEntity.ok(ApiResponse(null as Void?, "Warehouse deleted successfully"))
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
