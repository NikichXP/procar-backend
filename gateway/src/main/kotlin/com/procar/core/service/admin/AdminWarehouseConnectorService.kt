package com.procar.core.service.admin

import com.procar.provider.admin.AdminCreateWarehouseRequest
import com.procar.provider.admin.AdminUpdateWarehouseRequest
import com.procar.provider.admin.AdminWarehouseController
import com.procar.provider.admin.AdminWarehouseResponse
import com.procar.provider.common.ApiResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class AdminWarehouseConnectorService(
    @Qualifier("adminWarehouseHttpClient") private val adminWarehouseController: AdminWarehouseController
) {

    suspend fun getAllWarehouses(): ResponseEntity<ApiResponse<List<AdminWarehouseResponse>>> {
        return adminWarehouseController.getAllWarehouses()
    }

    suspend fun getWarehouse(warehouseId: String): ResponseEntity<ApiResponse<AdminWarehouseResponse>> {
        return adminWarehouseController.getWarehouse(warehouseId)
    }

    suspend fun createWarehouse(request: AdminCreateWarehouseRequest): ResponseEntity<ApiResponse<AdminWarehouseResponse>> {
        return adminWarehouseController.createWarehouse(request)
    }

    suspend fun updateWarehouse(warehouseId: String, request: AdminUpdateWarehouseRequest): ResponseEntity<ApiResponse<AdminWarehouseResponse>> {
        return adminWarehouseController.updateWarehouse(warehouseId, request)
    }

    suspend fun deleteWarehouse(warehouseId: String): ResponseEntity<ApiResponse<Void?>> {
        return adminWarehouseController.deleteWarehouse(warehouseId)
    }
}
