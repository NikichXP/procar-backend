package com.procar.core.api

import com.procar.core.service.admin.AdminLotConnectorService
import com.procar.provider.admin.AdminCreateLotRequest
import com.procar.provider.admin.AdminLotController
import com.procar.provider.admin.AdminLotResponse
import com.procar.provider.admin.AdminPaginatedLotsResponse
import com.procar.provider.admin.AdminUpdateLotRequest
import com.procar.provider.admin.AdminHiddenRequest
import com.procar.provider.admin.AdminUpdateStatusRequest
import com.procar.provider.lot.LotStatus
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/lots")
class AdminLotController(
    private val adminLotConnectorService: AdminLotConnectorService
) : AdminLotController {

    @PostMapping
    override fun createLot(@Valid @RequestBody request: AdminCreateLotRequest): ResponseEntity<AdminLotResponse> {
        return adminLotConnectorService.createLot(request)
    }

    @GetMapping("/{lotId}")
    override fun getLot(@PathVariable lotId: String): ResponseEntity<AdminLotResponse> {
        return adminLotConnectorService.getLot(lotId)
    }

    @PutMapping("/{lotId}")
    override fun updateLot(
        @PathVariable lotId: String,
        @Valid @RequestBody request: AdminUpdateLotRequest
    ): ResponseEntity<AdminLotResponse> {
        return adminLotConnectorService.updateLot(lotId, request)
    }

    @DeleteMapping("/{lotId}")
    override fun deleteLot(@PathVariable lotId: String): ResponseEntity<Void> {
        return adminLotConnectorService.deleteLot(lotId)
    }

    @PostMapping("/{lotId}/status")
    override fun updateLotStatus(
        @PathVariable lotId: String,
        @RequestBody request: AdminUpdateStatusRequest
    ): ResponseEntity<AdminLotResponse> {
        return adminLotConnectorService.updateLotStatus(lotId, request)
    }

    @GetMapping
    override fun getAllLots(
        @RequestParam cursor: String?,
        @RequestParam limit: Int,
        @RequestParam status: LotStatus?
    ): ResponseEntity<AdminPaginatedLotsResponse> {
        return adminLotConnectorService.getAllLots(cursor, limit, status)
    }

    @PostMapping("/{lotId}/hidden")
    override fun setHiddenStatus(
        @PathVariable lotId: String,
        @RequestBody request: AdminHiddenRequest
    ): ResponseEntity<AdminLotResponse> {
        return adminLotConnectorService.setHiddenStatus(lotId, request)
    }
}
