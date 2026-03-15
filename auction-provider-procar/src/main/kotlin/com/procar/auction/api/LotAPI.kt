package com.procar.auction.api

import com.procar.auction.factory.LotResponseFactory
import com.procar.auction.service.InternalAuctionLotService
import com.procar.provider.InternalLotAPI
import com.procar.provider.lot.AdvancedLotSearchRequest
import com.procar.provider.lot.LotSearchResponse
import com.procar.provider.lot.ProviderLot
import org.springframework.core.convert.ConversionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class LotAPI(
    private val lotService: InternalAuctionLotService,
    private val responseFactory: LotResponseFactory,
    private val conversionService: ConversionService
) : InternalLotAPI {

    override fun searchLots(request: AdvancedLotSearchRequest): ResponseEntity<LotSearchResponse> {
        return try {
            val response = lotService.searchLots(request)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.ok(responseFactory.createEmptySearchResponse())
        }
    }

    override fun getLotDetail(lotId: String): ResponseEntity<ProviderLot> {
        val lot = lotService.getLotById(lotId)
            ?: return ResponseEntity.notFound().build()

        val providerLot = conversionService.convert(lot, ProviderLot::class.java)
        return ResponseEntity.ok(providerLot)
    }
}
