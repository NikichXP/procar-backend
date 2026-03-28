package com.procar.provider.admin

import com.procar.provider.common.ApiResponse
import com.procar.provider.lot.LotStatus
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.service.annotation.DeleteExchange
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PutExchange
import org.springframework.web.bind.annotation.RequestParam

@HttpExchange("/api/admin/lots")
interface AdminLotController {

    @PostExchange
    fun createLot(@Valid @RequestBody request: AdminCreateLotRequest): ResponseEntity<ApiResponse<AdminLotResponse>>

    @GetExchange("/{lotId}")
    fun getLot(@PathVariable lotId: String): ResponseEntity<ApiResponse<AdminLotResponse>>

    @PutExchange("/{lotId}")
    fun updateLot(
        @PathVariable lotId: String,
        @Valid @RequestBody request: AdminUpdateLotRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>>

    @DeleteExchange("/{lotId}")
    fun deleteLot(@PathVariable lotId: String): ResponseEntity<ApiResponse<Void?>>

    @PostExchange("/{lotId}/status")
    fun updateLotStatus(
        @PathVariable lotId: String,
        @RequestBody request: AdminUpdateStatusRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>>

    @GetExchange
    fun getAllLots(
        @RequestParam cursor: String?,
        @RequestParam limit: Int,
        @RequestParam status: LotStatus?
    ): ResponseEntity<ApiResponse<AdminPaginatedLotsResponse>>

    @PostExchange("/{lotId}/hidden")
    fun setHiddenStatus(
        @PathVariable lotId: String,
        @RequestBody request: AdminHiddenRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>>
}
