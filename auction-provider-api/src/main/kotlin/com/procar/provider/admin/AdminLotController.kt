package com.procar.provider.admin

import com.procar.provider.common.ApiResponse
import com.procar.provider.lot.LotStatus
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.*

@HttpExchange("/api/admin/lots")
interface AdminLotController {

    @PostExchange
    suspend fun createLot(@Valid @RequestBody request: AdminCreateLotRequest): ResponseEntity<ApiResponse<AdminLotResponse>>

    @GetExchange("/{lotId}")
    suspend fun getLot(@PathVariable lotId: String): ResponseEntity<ApiResponse<AdminLotResponse>>

    @PutExchange("/{lotId}")
    suspend fun updateLot(
        @PathVariable lotId: String,
        @Valid @RequestBody request: AdminUpdateLotRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>>

    @DeleteExchange("/{lotId}")
    suspend fun deleteLot(@PathVariable lotId: String): ResponseEntity<ApiResponse<Void?>>

    @PostExchange("/{lotId}/status")
    suspend fun updateLotStatus(
        @PathVariable lotId: String,
        @RequestBody request: AdminUpdateStatusRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>>

    @GetExchange
    suspend fun getAllLots(
        @RequestParam cursor: String?,
        @RequestParam limit: Int,
        @RequestParam status: LotStatus?
    ): ResponseEntity<ApiResponse<AdminPaginatedLotsResponse>>

    @PostExchange("/{lotId}/hidden")
    suspend fun setHiddenStatus(
        @PathVariable lotId: String,
        @RequestBody request: AdminHiddenRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>>
}
