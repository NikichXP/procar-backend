package com.procar.core.api.admin

import com.procar.core.service.admin.AdminLotConnectorService
import com.procar.provider.admin.*
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
) {

    @PostMapping
    suspend fun createLot(@Valid @RequestBody request: GatewayCreateLotRequest): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.createLot(request)
    }

    @GetMapping("/{lotId}")
    suspend fun getLot(@PathVariable lotId: String): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.getLot(lotId)
    }

    @PutMapping("/{lotId}")
    suspend fun updateLot(
        @PathVariable lotId: String,
        @Valid @RequestBody request: AdminUpdateLotRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.updateLot(lotId, request)
    }

    @DeleteMapping("/{lotId}")
    suspend fun deleteLot(@PathVariable lotId: String): ResponseEntity<ApiResponse<Void?>> {
        return adminLotConnectorService.deleteLot(lotId)
    }

    @PostMapping("/{lotId}/status")
    suspend fun updateLotStatus(
        @PathVariable lotId: String,
        @RequestBody request: AdminUpdateStatusRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.updateLotStatus(lotId, request)
    }

    @GetMapping
    suspend fun getAllLots(
        @RequestParam cursor: String?,
        @RequestParam limit: Int,
        @RequestParam status: LotStatus?
    ): ResponseEntity<ApiResponse<AdminPaginatedLotsResponse>> {
        return adminLotConnectorService.getAllLots(cursor, limit, status)
    }

    @GetMapping("/{lotId}/possible-statuses")
    suspend fun getPossibleStatuses(
        @PathVariable lotId: String
    ): ResponseEntity<ApiResponse<List<String>>> {
        val response = adminLotConnectorService.getPossibleStatuses(lotId)
        val names = response.body?.data?.map { it.name } ?: emptyList()
        return ResponseEntity.ok(ApiResponse(names))
    }

    @PostMapping("/{lotId}/hidden")
    suspend fun setHiddenStatus(
        @PathVariable lotId: String,
        @RequestBody request: AdminHiddenRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.setHiddenStatus(lotId, request)
    }

    @PostMapping("/{lotId}/images")
    suspend fun addLotImage(
        @PathVariable lotId: String,
        @RequestBody request: AdminAddImageRequest
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.addLotImage(lotId, request)
    }

    @DeleteMapping("/{lotId}/images")
    suspend fun deleteLotImage(
        @PathVariable lotId: String,
        @RequestParam url: String
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.deleteLotImage(lotId, url)
    }

    @PostMapping("/{lotId}/photo", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun uploadLotPhoto(
        @PathVariable lotId: String,
        @RequestPart("file") filePart: FilePart
    ): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.uploadLotPhoto(lotId, filePart)
    }

    @PostMapping("/{lotId}/publish")
    suspend fun publishLot(@PathVariable lotId: String): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.publishLot(lotId)
    }

    @PostMapping("/{lotId}/unpublish")
    suspend fun unpublishLot(@PathVariable lotId: String): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.unpublishLot(lotId)
    }

    @PostMapping("/{lotId}/confirm-availability")
    suspend fun confirmAvailability(@PathVariable lotId: String): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotConnectorService.confirmAvailability(lotId)
    }
}
