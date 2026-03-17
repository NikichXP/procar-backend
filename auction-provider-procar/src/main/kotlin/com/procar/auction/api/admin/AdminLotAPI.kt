package com.procar.auction.api.admin

import com.procar.auction.document.LotDocument
import com.procar.auction.service.InternalAuctionLotService
import com.procar.provider.admin.AdminCreateLotRequest
import com.procar.provider.admin.AdminLotController
import com.procar.provider.admin.AdminLotResponse
import com.procar.provider.admin.AdminPaginatedLotsResponse
import com.procar.provider.admin.AdminUpdateLotRequest
import com.procar.provider.admin.AdminHiddenRequest
import com.procar.provider.admin.AdminUpdateStatusRequest
import com.procar.provider.lot.LotStatus
import org.springframework.core.convert.ConversionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminLotAPI(
    private val lotService: InternalAuctionLotService,
    private val conversionService: ConversionService
) : AdminLotController {

    override fun createLot(request: AdminCreateLotRequest): ResponseEntity<AdminLotResponse> {
        val lotDocument = conversionService.convert(request, LotDocument::class.java)!!
        val savedLot = lotService.createLot(lotDocument)
        val adminResponse = conversionService.convert(savedLot, AdminLotResponse::class.java)!!
        return ResponseEntity.ok(adminResponse)
    }

    override fun getLot(lotId: String): ResponseEntity<AdminLotResponse> {
        val lot = lotService.getLotById(lotId)
            ?: return ResponseEntity.notFound().build()
        val adminResponse = conversionService.convert(lot, AdminLotResponse::class.java)!!
        return ResponseEntity.ok(adminResponse)
    }

    override fun updateLot(
        lotId: String,
        request: AdminUpdateLotRequest
    ): ResponseEntity<AdminLotResponse> {
        val existingLot = lotService.getLotById(lotId)
            ?: return ResponseEntity.notFound().build()
        
        val updatedLot = conversionService.convert(Pair(request, existingLot), LotDocument::class.java)!!
        val savedLot = lotService.updateLot(lotId, updatedLot)
        val adminResponse = conversionService.convert(savedLot, AdminLotResponse::class.java)!!
        return ResponseEntity.ok(adminResponse)
    }

    override fun deleteLot(lotId: String): ResponseEntity<Void> {
        val success = lotService.deleteLot(lotId)
        return if (success) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    override fun updateLotStatus(
        lotId: String,
        request: AdminUpdateStatusRequest
    ): ResponseEntity<AdminLotResponse> {
        val updatedLot = lotService.updateLotStatus(lotId, request.status)
            ?: return ResponseEntity.notFound().build()
        val adminResponse = conversionService.convert(updatedLot, AdminLotResponse::class.java)!!
        return ResponseEntity.ok(adminResponse)
    }

    override fun getAllLots(
        cursor: String?,
        limit: Int,
        status: LotStatus?
    ): ResponseEntity<AdminPaginatedLotsResponse> {
        val lots = lotService.getAllLots(cursor, limit, status)
        return ResponseEntity.ok(lots)
    }

    override fun setHiddenStatus(
        lotId: String,
        request: AdminHiddenRequest
    ): ResponseEntity<AdminLotResponse> {
        val updatedLot = if (request.hidden) {
            lotService.archiveLot(lotId)
        } else {
            lotService.unarchiveLot(lotId)
        }
        updatedLot ?: return ResponseEntity.notFound().build()
        val adminResponse = conversionService.convert(updatedLot, AdminLotResponse::class.java)!!
        return ResponseEntity.ok(adminResponse)
    }
}
