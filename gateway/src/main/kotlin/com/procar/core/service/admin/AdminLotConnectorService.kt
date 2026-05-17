package com.procar.core.service.admin

import com.procar.core.api.admin.GatewayCreateLotRequest
import com.procar.core.service.StorageService
import com.procar.provider.admin.*
import com.procar.provider.common.ApiResponse
import com.procar.provider.lot.LotStatus
import com.procar.provider.lot.SellerType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AdminLotConnectorService(
    @Qualifier("adminLotHttpClient") private val adminLotController: AdminLotController,
    private val storageService: StorageService,
    private val adminBrokerConnectorService: AdminBrokerConnectorService,
    private val adminWarehouseConnectorService: AdminWarehouseConnectorService,
) {

    suspend fun createLot(request: GatewayCreateLotRequest): ResponseEntity<ApiResponse<AdminLotResponse>> {
        val broker = try {
            adminBrokerConnectorService.getBroker(request.brokerId)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Broker '${request.brokerId}' not found", e)
        }

        val warehouseResponse = adminWarehouseConnectorService.getWarehouse(request.warehouseId)
        val warehouse = warehouseResponse.body?.data
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Warehouse '${request.warehouseId}' not found")

        val providerRequest = AdminCreateLotRequest(
            externalId = request.externalId,
            title = request.title,
            description = request.description,
            vehicle = request.vehicle,
            auction = request.auction,
            location = AdminLocationInfoRequest(
                address = warehouse.address,
                city = warehouse.city,
                state = warehouse.state,
                zipCode = warehouse.zipCode,
                country = warehouse.country,
                coordinates = warehouse.coordinates,
                timezone = warehouse.timezone,
            ),
            metadata = AdminLotMetadataRequest(
                tags = request.metadata.tags,
                categories = request.metadata.categories,
                sellerInfo = AdminSellerInfoRequest(
                    id = broker.id,
                    name = broker.companyName,
                    type = SellerType.DEALER,
                ),
                inspection = request.metadata.inspection,
                history = request.metadata.history,
                fees = request.metadata.fees,
                shipping = request.metadata.shipping,
            ),
            status = request.status,
            brokerOrgId = broker.id,
            lotType = request.lotType,
            buyoutPrice = request.buyoutPrice,
            brand = request.brand,
        )
        return adminLotController.createLot(providerRequest)
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

    suspend fun getPossibleStatuses(lotId: String): ResponseEntity<ApiResponse<List<LotStatus>>> {
        return adminLotController.getPossibleStatuses(lotId)
    }

    suspend fun publishLot(lotId: String): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotController.publishLot(lotId)
    }

    suspend fun unpublishLot(lotId: String): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotController.unpublishLot(lotId)
    }

    suspend fun confirmAvailability(lotId: String): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotController.confirmAvailability(lotId)
    }

    suspend fun setHiddenStatus(lotId: String, request: AdminHiddenRequest): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotController.setHiddenStatus(lotId, request)
    }

    suspend fun addLotImage(lotId: String, request: AdminAddImageRequest): ResponseEntity<ApiResponse<AdminLotResponse>> {
        return adminLotController.addLotImage(lotId, request)
    }

    suspend fun deleteLotImage(lotId: String, url: String): ResponseEntity<ApiResponse<AdminLotResponse>> {
        val response = adminLotController.deleteLotImage(lotId, url)
        if (response.statusCode.is2xxSuccessful) {
            runCatching { storageService.delete(url) }
        }
        return response
    }

    suspend fun uploadLotPhoto(lotId: String, filePart: FilePart): ResponseEntity<ApiResponse<AdminLotResponse>> {
        val key = storageService.upload(filePart)
        return adminLotController.addLotImage(
            lotId,
            AdminAddImageRequest(
                url = key,
                description = filePart.filename()
            )
        )
    }
}
