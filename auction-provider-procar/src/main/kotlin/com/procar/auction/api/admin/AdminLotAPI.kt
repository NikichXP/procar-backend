package com.procar.auction.api.admin

import com.procar.auction.document.LotEntity
import com.procar.auction.document.VehicleImageDocument
import com.procar.auction.service.InternalAuctionLotService
import com.procar.auction.service.status.LotStatusHelper
import com.procar.provider.admin.*
import com.procar.provider.common.ApiResponse
import com.procar.provider.lot.LotStatus
import org.springframework.core.convert.ConversionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class AdminLotAPI(
    private val lotService: InternalAuctionLotService,
    private val lotStatusHelper: LotStatusHelper,
    private val conversionService: ConversionService
) : AdminLotController {

    override suspend fun createLot(request: AdminCreateLotRequest): ResponseEntity<ApiResponse<AdminLotResponse>> {
        val lotEntity = conversionService.convert(request, LotEntity::class.java)!!
        val savedLot = lotService.createLot(lotEntity)
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

        val newStatus = request.status
        if (newStatus != null && newStatus != existingLot.status) {
            if (!lotStatusHelper.canMigrateToStatus(existingLot, newStatus)) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status transition from ${existingLot.status} to ${newStatus} is not allowed"
                )
            }
        }

        val updatedLot = conversionService.convert(Pair(request, existingLot), LotEntity::class.java)!!
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
        val existingLot = lotService.getLotById(lotId)
            ?: return ResponseEntity.notFound().build()

        if (!lotStatusHelper.canMigrateToStatus(existingLot, request.status)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Status transition from ${existingLot.status} to ${request.status} is not allowed"
            )
        }

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

    override suspend fun deleteLotImage(
        lotId: String,
        url: String
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        val updatedLot = lotService.removeLotImage(lotId, url)
            ?: return ResponseEntity.notFound().build()
        val adminResponse = conversionService.convert(updatedLot, AdminLotResponse::class.java)!!
        return ResponseEntity.ok(ApiResponse(adminResponse, "Image removed successfully"))
    }

    override suspend fun getPossibleStatuses(
        lotId: String
    ): ResponseEntity<ApiResponse<List<LotStatus>>> {
        val lot = lotService.getLotById(lotId)
            ?: return ResponseEntity.notFound().build()
        val statuses = lotStatusHelper.getPossibleStatuses(lot)
        return ResponseEntity.ok(ApiResponse(statuses))
    }
}
