package com.procar.auction.api

import com.procar.auction.factory.LotResponseFactory
import com.procar.auction.service.InternalAuctionLotService
import com.procar.provider.InternalLotAPI
import com.procar.provider.common.ApiResponse
import com.procar.provider.lot.AdvancedLotSearchRequest
import com.procar.provider.lot.LotSearchResponse
import com.procar.provider.lot.VehicleLot
import org.springframework.core.convert.ConversionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class LotAPI(
    private val lotService: InternalAuctionLotService,
    private val responseFactory: LotResponseFactory,
    private val conversionService: ConversionService
) : InternalLotAPI {

    override suspend fun searchLots(request: AdvancedLotSearchRequest): ResponseEntity<ApiResponse<LotSearchResponse>> {
        return try {
            val response = lotService.searchLots(request)
            ResponseEntity.ok(ApiResponse(response))
        } catch (e: Exception) {
            println("Error in searchLots: ${e.message}")
            e.printStackTrace()
            ResponseEntity.ok(ApiResponse(responseFactory.createEmptySearchResponse()))
        }
    }

    override suspend fun getLotDetail(lotId: String): ResponseEntity<ApiResponse<VehicleLot>> {
        val lot = lotService.getLotById(lotId)
            ?: return ResponseEntity.notFound().build()

        val providerLot = conversionService.convert(lot, VehicleLot::class.java)!!
        return ResponseEntity.ok(ApiResponse(providerLot))
    }
}
