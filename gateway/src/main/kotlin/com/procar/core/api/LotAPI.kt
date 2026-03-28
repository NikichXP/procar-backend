package com.procar.core.api

import com.procar.core.api.dto.CarCondition
import com.procar.core.api.dto.LotDetail
import com.procar.core.api.dto.LotPage
import com.procar.core.api.dto.LotSearchRequest
import com.procar.core.api.dto.LotStatus
import com.procar.core.service.LotService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lots")
class LotAPI(
    private val lotService: LotService
) {

    @GetMapping
    suspend fun getLots(
        @RequestParam(required = false) status: LotStatus?,
        @RequestParam(required = false) source: String?,
        @RequestParam(required = false) endsInMinutes: Int?,
        @RequestParam(required = false) brandId: String?,
        @RequestParam(required = false) modelId: String?,
        @RequestParam(required = false) yearFrom: Int?,
        @RequestParam(required = false) yearTo: Int?,
        @RequestParam(required = false) priceFrom: Double?,
        @RequestParam(required = false) priceTo: Double?,
        @RequestParam(required = false) condition: CarCondition?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "endTime,asc") sort: String
    ): LotPage {
        val request = LotSearchRequest(
            status = status,
            source = source,
            endsInMinutes = endsInMinutes,
            brandId = brandId,
            modelId = modelId,
            yearFrom = yearFrom,
            yearTo = yearTo,
            priceFrom = priceFrom,
            priceTo = priceTo,
            condition = condition,
            offset = page * size,
            limit = size,
            sort = sort
        )
        
        val lots = lotService.getLots(request)
        val totalElements = lotService.countLots(request)
        val totalPages = (totalElements + size - 1) / size
        
        return LotPage(
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
            content = lots
        )
    }

    @GetMapping("/{lotId}")
    suspend fun getLotDetail(@PathVariable lotId: String): LotDetail {
        return lotService.getLotDetail(lotId)
    }
}
