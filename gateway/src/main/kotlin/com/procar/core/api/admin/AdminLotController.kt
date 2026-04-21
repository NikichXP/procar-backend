package com.procar.core.api.admin

import com.procar.core.service.admin.AdminLotConnectorService
import com.procar.provider.admin.*
import com.procar.provider.admin.AdminLotController
import com.procar.provider.common.ApiResponse
import com.procar.provider.lot.LotStatus
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/lots")
class AdminLotController(
    private val adminLotConnectorService: AdminLotConnectorService
) : AdminLotController {

    @PostMapping
    override suspend fun createLot(@Valid @RequestBody request: AdminCreateLotRequest): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.createLot(request)
    }

    @GetMapping("/{lotId}")
    override suspend fun getLot(@PathVariable lotId: String): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.getLot(lotId)
    }

    @PutMapping("/{lotId}")
    override suspend fun updateLot(
        @PathVariable lotId: String,
        @Valid @RequestBody request: AdminUpdateLotRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.updateLot(lotId, request)
    }

    @DeleteMapping("/{lotId}")
    override suspend fun deleteLot(@PathVariable lotId: String): ResponseEntity<ApiResponse<Void?>> {
        return adminLotConnectorService.deleteLot(lotId)
    }

    @PostMapping("/{lotId}/status")
    override suspend fun updateLotStatus(
        @PathVariable lotId: String,
        @RequestBody request: AdminUpdateStatusRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.updateLotStatus(lotId, request)
    }

    @GetMapping
    override suspend fun getAllLots(
        @RequestParam cursor: String?,
        @RequestParam limit: Int,
        @RequestParam status: LotStatus?
    ): ResponseEntity<ApiResponse<AdminPaginatedLotsResponse>> {
        return adminLotConnectorService.getAllLots(cursor, limit, status)
    }

    @PostMapping("/{lotId}/hidden")
    override suspend fun setHiddenStatus(
        @PathVariable lotId: String,
        @RequestBody request: AdminHiddenRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.setHiddenStatus(lotId, request)
    }

    @PostMapping("/{lotId}/images")
    override suspend fun addLotImage(
        @PathVariable lotId: String,
        @RequestBody request: AdminAddImageRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.addLotImage(lotId, request)
    }

    @PostMapping("/{lotId}/photo", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun uploadLotPhoto(
        @PathVariable lotId: String,
        @RequestPart("file") filePart: FilePart
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.uploadLotPhoto(lotId, filePart)
    }
}
