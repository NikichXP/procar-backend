package com.procar.core.api

import com.procar.core.api.dto.LotDetail
import com.procar.core.api.dto.LotPage
import com.procar.core.api.dto.LotStatus
import com.procar.core.api.dto.CarCondition
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lots")
class LotAPI {

    @GetMapping
    fun getLots(
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
        TODO("Implement lots listing with filters")
    }

    @GetMapping("/{lotId}")
    fun getLotDetail(@PathVariable lotId: String): LotDetail {
        TODO("Implement lot detail retrieval")
    }
}
