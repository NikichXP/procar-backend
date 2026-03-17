package com.procar.provider.admin

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

@RequestMapping("/api/admin/lots")
interface AdminLotController {

    @PostMapping
    fun createLot(@Valid @RequestBody request: AdminCreateLotRequest): ResponseEntity<AdminLotResponse>

    @GetMapping("/{lotId}")
    fun getLot(@PathVariable lotId: String): ResponseEntity<AdminLotResponse>

    @PutMapping("/{lotId}")
    fun updateLot(
        @PathVariable lotId: String,
        @Valid @RequestBody request: AdminUpdateLotRequest
    ): ResponseEntity<AdminLotResponse>

    @DeleteMapping("/{lotId}")
    fun deleteLot(@PathVariable lotId: String): ResponseEntity<Void>

    @PostMapping("/{lotId}/status")
    fun updateLotStatus(
        @PathVariable lotId: String,
        @RequestBody request: AdminUpdateStatusRequest
    ): ResponseEntity<AdminLotResponse>

    @GetMapping
    fun getAllLots(
        @RequestParam cursor: String?,
        @RequestParam limit: Int,
        @RequestParam status: LotStatus?
    ): ResponseEntity<AdminPaginatedLotsResponse>

    @PostMapping("/{lotId}/archive")
    fun archiveLot(@PathVariable lotId: String): ResponseEntity<AdminLotResponse>

    @PostMapping("/{lotId}/unarchive")
    fun unarchiveLot(@PathVariable lotId: String): ResponseEntity<AdminLotResponse>
}
