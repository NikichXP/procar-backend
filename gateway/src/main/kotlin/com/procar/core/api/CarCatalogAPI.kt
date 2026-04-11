package com.procar.core.api

import com.procar.core.api.dto.Brand
import com.procar.core.api.dto.Model
import com.procar.core.service.CatalogService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "car-catalog-api", description = "Browse car brands and models available in the catalog")
@RestController
@RequestMapping("/catalog")
class CarCatalogAPI(
    private val catalogService: CatalogService
) {

    @Operation(summary = "List brands", description = "Returns all car brands, optionally filtered by name query.")
    @GetMapping("/brand")
    fun getBrands(
        @Parameter(
            description = "Filter brands by name",
            example = "Toyota"
        ) @RequestParam(required = false) query: String?
    ): List<Brand> {
        return catalogService.getBrands(query)
    }

    @Operation(
        summary = "List models for a brand",
        description = "Returns all models for the given brand, optionally filtered by name query."
    )
    @GetMapping("/brand/{brandId}/models")
    fun getBrandModels(
        @Parameter(
            description = "Brand ID",
            example = "01956b0a-aaaa-7000-8000-000000000001"
        ) @PathVariable brandId: String,
        @Parameter(
            description = "Filter models by name",
            example = "Camry"
        ) @RequestParam(required = false) query: String?
    ): List<Model> {
        return catalogService.getBrandModels(brandId, query)
    }
}