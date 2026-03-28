package com.procar.provider.admin

import com.procar.provider.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.*

@HttpExchange("/api/admin/warehouses")
interface AdminWarehouseController {

    @GetExchange
    suspend fun getAllWarehouses(): ResponseEntity<ApiResponse<List<AdminWarehouseResponse>>>

    @GetExchange("/{warehouseId}")
    suspend fun getWarehouse(@PathVariable warehouseId: String): ResponseEntity<ApiResponse<AdminWarehouseResponse>>

    @PostExchange
    suspend fun createWarehouse(@RequestBody request: AdminCreateWarehouseRequest): ResponseEntity<ApiResponse<AdminWarehouseResponse>>

    @PutExchange("/{warehouseId}")
    suspend fun updateWarehouse(
        @PathVariable warehouseId: String,
        @RequestBody request: AdminUpdateWarehouseRequest
    ): ResponseEntity<ApiResponse<AdminWarehouseResponse>>

    @DeleteExchange("/{warehouseId}")
    suspend fun deleteWarehouse(@PathVariable warehouseId: String): ResponseEntity<ApiResponse<Void?>>
}
