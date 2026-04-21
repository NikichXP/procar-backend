package com.procar.auction.api.admin

import com.procar.auction.document.LotDocument
import com.procar.auction.document.VehicleImageDocument
import com.procar.auction.service.InternalAuctionLotService
import com.procar.provider.admin.*
import com.procar.provider.common.ApiResponse
import com.procar.provider.lot.LotStatus
import org.springframework.core.convert.ConversionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminLotAPI(
    private val lotService: InternalAuctionLotService,
    private val conversionService: ConversionService
) : AdminLotController {

    override suspend fun createLot(request: AdminCreateLotRequest): ResponseEntity<ApiResponse<AdminLotResponse>> {
        val lotDocument = conversionService.convert(request, LotDocument::class.java)!!
        val savedLot = lotService.createLot(lotDocument)
        val adminResponse = conversionService.convert(savedLot, AdminLotResponse::class.java)!!
        return ResponseEntity.ok(ApiResponse(adminResponse, "Lot created successfully"))
    }

    override suspend fun getLot(lotId: String): ResponseEntity<ApiResponse<AdminLotResponse>> {
        val lot = lotService.getLotById(lotId)
            ?: return ResponseEntity.notFound().build()
        val adminResponse = conversionService.convert(lot, AdminLotResponse::class.java)!!
        return ResponseEntity.ok(ApiResponse(adminResponse))
    }

    override suspend fun updateLot(
        lotId: String,
        request: AdminUpdateLotRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        val existingLot = lotService.getLotById(lotId)
            ?: return ResponseEntity.notFound().build()
        
        val updatedLot = conversionService.convert(Pair(request, existingLot), LotDocument::class.java)!!
        val savedLot = lotService.updateLot(lotId, updatedLot)
        val adminResponse = conversionService.convert(savedLot, AdminLotResponse::class.java)!!
        return ResponseEntity.ok(ApiResponse(adminResponse, "Lot updated successfully"))
    }

    override suspend fun deleteLot(lotId: String): ResponseEntity<ApiResponse<Void?>> {
        val success = lotService.deleteLot(lotId)
        return if (success) {
            ResponseEntity.ok(ApiResponse(null as Void?, "Lot deleted successfully"))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    override suspend fun updateLotStatus(
        lotId: String,
        request: AdminUpdateStatusRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        val updatedLot = lotService.updateLotStatus(lotId, request.status)
            ?: return ResponseEntity.notFound().build()
        val adminResponse = conversionService.convert(updatedLot, AdminLotResponse::class.java)!!
        return ResponseEntity.ok(ApiResponse(adminResponse, "Lot status updated successfully"))
    }

    override suspend fun getAllLots(
        cursor: String?,
        limit: Int,
        status: LotStatus?
    ): ResponseEntity<ApiResponse<AdminPaginatedLotsResponse>> {
        val lots = lotService.getAllLots(cursor, limit, status)
        return ResponseEntity.ok(ApiResponse(lots))
    }

    override suspend fun setHiddenStatus(
        lotId: String,
        request: AdminHiddenRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        val updatedLot = if (request.hidden) {
            lotService.archiveLot(lotId)
        } else {
            lotService.unarchiveLot(lotId)
        }
        updatedLot ?: return ResponseEntity.notFound().build()
        val adminResponse = conversionService.convert(updatedLot, AdminLotResponse::class.java)!!
        val message = if (request.hidden) "Lot hidden successfully" else "Lot unhidden successfully"
        return ResponseEntity.ok(ApiResponse(adminResponse, message))
    }

    override suspend fun addLotImage(
        lotId: String,
        request: AdminAddImageRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        val updatedLot = lotService.addLotImage(
            lotId,
            VehicleImageDocument(
                url = request.url,
                type = request.type,
                description = request.description,
                isPrimary = request.isPrimary
            )
        ) ?: return ResponseEntity.notFound().build()
        val adminResponse = conversionService.convert(updatedLot, AdminLotResponse::class.java)!!
        return ResponseEntity.ok(ApiResponse(adminResponse, "Image added successfully"))
    }
}
