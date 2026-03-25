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

    fun createLot(request: AdminCreateLotRequest): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotController.createLot(request)
    }

    fun getLot(lotId: String): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotController.getLot(lotId)
    }

    fun updateLot(lotId: String, request: AdminUpdateLotRequest): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotController.updateLot(lotId, request)
    }

    fun deleteLot(lotId: String): ResponseEntity<ApiResponse<Void?>> {
        return adminLotController.deleteLot(lotId)
    }

    fun updateLotStatus(lotId: String, request: AdminUpdateStatusRequest): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotController.updateLotStatus(lotId, request)
    }

    fun getAllLots(cursor: String?, limit: Int, status: LotStatus?): ResponseEntity<ApiResponse<AdminPaginatedLotsResponse>> {
        return adminLotController.getAllLots(cursor, limit, status)
    }

    fun setHiddenStatus(lotId: String, request: AdminHiddenRequest): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotController.setHiddenStatus(lotId, request)
    }
}
