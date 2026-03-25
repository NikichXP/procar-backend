package com.procar.core.api

import com.procar.core.service.admin.AdminLotConnectorService
import com.procar.provider.admin.*
import com.procar.provider.common.ApiResponse
import com.procar.provider.lot.LotStatus
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/lots")
class GatewayAdminLotController(
    private val adminLotConnectorService: AdminLotConnectorService
) : AdminLotController {

    @PostMapping
    override fun createLot(@Valid @RequestBody request: AdminCreateLotRequest): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.createLot(request)
    }

    @GetMapping("/{lotId}")
    override fun getLot(@PathVariable lotId: String): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.getLot(lotId)
    }

    @PutMapping("/{lotId}")
    override fun updateLot(
        @PathVariable lotId: String,
        @Valid @RequestBody request: AdminUpdateLotRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.updateLot(lotId, request)
    }

    @DeleteMapping("/{lotId}")
    override fun deleteLot(@PathVariable lotId: String): ResponseEntity<ApiResponse<Void?>> {
        return adminLotConnectorService.deleteLot(lotId)
    }

    @PostMapping("/{lotId}/status")
    override fun updateLotStatus(
        @PathVariable lotId: String,
        @RequestBody request: AdminUpdateStatusRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.updateLotStatus(lotId, request)
    }

    @GetMapping
    override fun getAllLots(
        @RequestParam cursor: String?,
        @RequestParam limit: Int,
        @RequestParam status: LotStatus?
    ): ResponseEntity<ApiResponse<AdminPaginatedLotsResponse>> {
        return adminLotConnectorService.getAllLots(cursor, limit, status)
    }

    @PostMapping("/{lotId}/hidden")
    override fun setHiddenStatus(
        @PathVariable lotId: String,
        @RequestBody request: AdminHiddenRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.setHiddenStatus(lotId, request)
    }
}
