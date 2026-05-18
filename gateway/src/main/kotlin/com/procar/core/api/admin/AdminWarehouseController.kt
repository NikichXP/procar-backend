package com.procar.core.api.admin

import com.procar.core.service.admin.AdminWarehouseConnectorService
import com.procar.provider.admin.AdminCreateWarehouseRequest
import com.procar.provider.admin.AdminUpdateWarehouseRequest
import com.procar.provider.admin.AdminWarehouseController
import com.procar.provider.admin.AdminWarehouseResponse
import com.procar.provider.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/warehouses")
class AdminWarehouseController(
    private val adminWarehouseConnectorService: AdminWarehouseConnectorService
) : AdminWarehouseController {

    @GetMapping
    override suspend fun getAllWarehouses(): ResponseEntity<ApiResponse<List<AdminWarehouseResponse>>> {
        return adminWarehouseConnectorService.getAllWarehouses()
    }

    @GetMapping("/{warehouseId}")
    override suspend fun getWarehouse(@PathVariable warehouseId: String): ResponseEntity<ApiResponse<AdminWarehouseResponse>> {
        return adminWarehouseConnectorService.getWarehouse(warehouseId)
    }

    @PostMapping
    override suspend fun createWarehouse(@RequestBody request: AdminCreateWarehouseRequest): ResponseEntity<ApiResponse<AdminWarehouseResponse>> {
        return adminWarehouseConnectorService.createWarehouse(request)
    }

    @PutMapping("/{warehouseId}")
    override suspend fun updateWarehouse(
        @PathVariable warehouseId: String,
        @RequestBody request: AdminUpdateWarehouseRequest
    ): ResponseEntity<ApiResponse<AdminWarehouseResponse>> {
        return adminWarehouseConnectorService.updateWarehouse(warehouseId, request)
    }

    @DeleteMapping("/{warehouseId}")
    override suspend fun deleteWarehouse(@PathVariable warehouseId: String): ResponseEntity<ApiResponse<Void?>> {
        return adminWarehouseConnectorService.deleteWarehouse(warehouseId)
    }
}
