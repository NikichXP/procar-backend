package com.procar.auction.api.admin

import com.procar.auction.service.InternalAuctionLotService
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
class AdminLotAPI(
    private val lotService: InternalAuctionLotService
) {

    @PostMapping
    fun createLot(@Valid @RequestBody request: AdminCreateLotRequest): ResponseEntity<AdminLotResponse> {
        val lotDocument = request.toLotDocument()
        val savedLot = lotService.createLot(lotDocument)
        return ResponseEntity.ok(AdminLotResponse.fromLotDocument(savedLot))
    }

    @GetMapping("/{lotId}")
    fun getLot(@PathVariable lotId: String): ResponseEntity<AdminLotResponse> {
        val lot = lotService.getLotById(lotId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(AdminLotResponse.fromLotDocument(lot))
    }

    @PutMapping("/{lotId}")
    fun updateLot(
        @PathVariable lotId: String,
        @Valid @RequestBody request: AdminUpdateLotRequest
    ): ResponseEntity<AdminLotResponse> {
        val existingLot = lotService.getLotById(lotId)
            ?: return ResponseEntity.notFound().build()
        
        val updatedLot = lotService.updateLot(lotId, request.toLotDocument(existingLot))
        return ResponseEntity.ok(AdminLotResponse.fromLotDocument(updatedLot))
    }

    @DeleteMapping("/{lotId}")
    fun deleteLot(@PathVariable lotId: String): ResponseEntity<Void> {
        val success = lotService.deleteLot(lotId)
        return if (success) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{lotId}/status")
    fun updateLotStatus(
        @PathVariable lotId: String,
        @RequestBody request: AdminUpdateStatusRequest
    ): ResponseEntity<AdminLotResponse> {
        val updatedLot = lotService.updateLotStatus(lotId, request.status)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(AdminLotResponse.fromLotDocument(updatedLot))
    }

    @GetMapping
    fun getAllLots(
        @RequestParam cursor: String? = null,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam status: LotStatus?
    ): ResponseEntity<AdminPaginatedLotsResponse> {
        val lots = lotService.getAllLots(cursor, limit, status)
        return ResponseEntity.ok(lots)
    }

    @PostMapping("/{lotId}/archive")
    fun archiveLot(@PathVariable lotId: String): ResponseEntity<AdminLotResponse> {
        val archivedLot = lotService.archiveLot(lotId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(AdminLotResponse.fromLotDocument(archivedLot))
    }

    @PostMapping("/{lotId}/unarchive")
    fun unarchiveLot(@PathVariable lotId: String): ResponseEntity<AdminLotResponse> {
        val unarchivedLot = lotService.unarchiveLot(lotId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(AdminLotResponse.fromLotDocument(unarchivedLot))
    }
}
