package com.procar.core.api

import com.procar.core.api.dto.Brand
import com.procar.core.api.dto.Model
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/catalog")
class CarCatalogAPI {

    @GetMapping("/brand")
    fun getBrands(@RequestParam(required = false) query: String?): List<Brand> {
        TODO("Implement brand listing with optional search")
    }

    @GetMapping("/brand/{brandId}/models")
    fun getBrandModels(
        @PathVariable brandId: String,
        @RequestParam(required = false) query: String?
    ): List<Model> {
        TODO("Implement models listing for brand")
    }
}