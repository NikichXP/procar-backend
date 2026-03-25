package com.procar.auction.api

import com.procar.auction.factory.LotResponseFactory
import com.procar.auction.service.InternalAuctionLotService
import com.procar.provider.InternalLotAPI
import com.procar.provider.lot.*
import com.procar.provider.common.ApiResponse
import org.springframework.core.convert.ConversionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class LotAPI(
    private val lotService: InternalAuctionLotService,
    private val responseFactory: LotResponseFactory,
    private val conversionService: ConversionService
) : InternalLotAPI {

    override fun searchLots(request: AdvancedLotSearchRequest): ResponseEntity<ApiResponse<LotSearchResponse>> {
        return try {
            val response = lotService.searchLots(request)
            ResponseEntity.ok(ApiResponse(response))
        } catch (_: Exception) {
            ResponseEntity.ok(ApiResponse(responseFactory.createEmptySearchResponse()))
        }
    }

    override fun getLotDetail(lotId: String): ResponseEntity<ApiResponse<VehicleLot>> {
        val lot = lotService.getLotById(lotId)
            ?: return ResponseEntity.notFound().build()

        val providerLot = conversionService.convert(lot, VehicleLot::class.java)!!
        return ResponseEntity.ok(ApiResponse(providerLot))
    }
}
