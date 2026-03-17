package com.procar.core.service.admin

import com.procar.provider.admin.AdminCreateLotRequest
import com.procar.provider.admin.AdminLotController
import com.procar.provider.admin.AdminLotResponse
import com.procar.provider.admin.AdminPaginatedLotsResponse
import com.procar.provider.admin.AdminUpdateLotRequest
import com.procar.provider.admin.AdminHiddenRequest
import com.procar.provider.admin.AdminUpdateStatusRequest
import com.procar.provider.lot.LotStatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class AdminLotConnectorService(
    @Qualifier("adminLotHttpClient") private val adminLotController: AdminLotController
) {

    fun createLot(request: AdminCreateLotRequest): ResponseEntity<AdminLotResponse> {
        return adminLotController.createLot(request)
    }

    fun getLot(lotId: String): ResponseEntity<AdminLotResponse> {
        return adminLotController.getLot(lotId)
    }

    fun updateLot(lotId: String, request: AdminUpdateLotRequest): ResponseEntity<AdminLotResponse> {
        return adminLotController.updateLot(lotId, request)
    }

    fun deleteLot(lotId: String): ResponseEntity<Void> {
        return adminLotController.deleteLot(lotId)
    }

    fun updateLotStatus(lotId: String, request: AdminUpdateStatusRequest): ResponseEntity<AdminLotResponse> {
        return adminLotController.updateLotStatus(lotId, request)
    }

    fun getAllLots(cursor: String?, limit: Int, status: LotStatus?): ResponseEntity<AdminPaginatedLotsResponse> {
        return adminLotController.getAllLots(cursor, limit, status)
    }

    fun setHiddenStatus(lotId: String, request: AdminHiddenRequest): ResponseEntity<AdminLotResponse> {
        return adminLotController.setHiddenStatus(lotId, request)
    }
}
