package com.procar.core.service.admin

import com.procar.provider.admin.*
import com.procar.provider.common.ApiResponse
import com.procar.provider.lot.LotStatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class AdminLotConnectorService(
    @Qualifier("adminLotHttpClient") private val adminLotController: AdminLotController
) {

    suspend fun createLot(request: AdminCreateLotRequest): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotController.createLot(request)
    }

    suspend fun getLot(lotId: String): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotController.getLot(lotId)
    }

    suspend fun updateLot(lotId: String, request: AdminUpdateLotRequest): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotController.updateLot(lotId, request)
    }

    suspend fun deleteLot(lotId: String): ResponseEntity<ApiResponse<Void?>> {
        return adminLotController.deleteLot(lotId)
    }

    suspend fun updateLotStatus(lotId: String, request: AdminUpdateStatusRequest): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotController.updateLotStatus(lotId, request)
    }

    suspend fun getAllLots(cursor: String?, limit: Int, status: LotStatus?): ResponseEntity<ApiResponse<AdminPaginatedLotsResponse>> {
        return adminLotController.getAllLots(cursor, limit, status)
    }

    suspend fun setHiddenStatus(lotId: String, request: AdminHiddenRequest): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotController.setHiddenStatus(lotId, request)
    }
}
