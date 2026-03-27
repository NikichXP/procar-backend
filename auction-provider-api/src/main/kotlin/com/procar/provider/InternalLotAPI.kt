package com.procar.provider

import com.procar.provider.common.*
import com.procar.provider.lot.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/internal/lots")
interface InternalLotAPI {
    
    @PostMapping("/search")
    fun searchLots(
        @RequestBody request: AdvancedLotSearchRequest
    ): ResponseEntity<ApiResponse<LotSearchResponse>>
    
    @GetMapping("/{lotId}")
    fun getLotDetail(
        @PathVariable lotId: String
    ): ResponseEntity<ApiResponse<VehicleLot>>
}