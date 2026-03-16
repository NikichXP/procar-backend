package com.procar.core.api

import com.procar.core.api.dto.Brand
import com.procar.core.api.dto.Model
import com.procar.core.service.CatalogService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/catalog")
class CarCatalogAPI(
    private val catalogService: CatalogService
) {

    @GetMapping("/brand")
    fun getBrands(@RequestParam(required = false) query: String?): List<Brand> {
        return catalogService.getBrands(query)
    }

    @GetMapping("/brand/{brandId}/models")
    fun getBrandModels(
        @PathVariable brandId: String,
        @RequestParam(required = false) query: String?
    ): List<Model> {
        return catalogService.getBrandModels(brandId, query)
    }
}