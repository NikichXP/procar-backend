package com.procar.provider

import com.procar.provider.common.*
import com.procar.provider.lot.*
import org.springframework.http.ResponseEntity
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange

@HttpExchange("/internal/lots")
interface InternalLotAPI {
    
    @PostExchange("/search")
    fun searchLots(
        @RequestBody request: AdvancedLotSearchRequest
    ): ResponseEntity<ApiResponse<LotSearchResponse>>
    
    @GetExchange("/{lotId}")
    fun getLotDetail(
        @PathVariable lotId: String
    ): ResponseEntity<ApiResponse<VehicleLot>>
}