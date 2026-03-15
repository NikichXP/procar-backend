package com.procar.provider

import com.procar.provider.lot.AdvancedLotSearchRequest
import com.procar.provider.lot.LotSearchResponse
import com.procar.provider.lot.ProviderLot
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/lots")
interface InternalLotAPI {
    
    @PostMapping("/search")
    fun searchLots(
        @RequestBody request: AdvancedLotSearchRequest
    ): ResponseEntity<LotSearchResponse>
    
    @GetMapping("/{lotId}")
    fun getLotDetail(
        @PathVariable lotId: String
    ): ResponseEntity<ProviderLot>
}