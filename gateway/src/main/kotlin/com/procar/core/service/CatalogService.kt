package com.procar.core.service

import com.procar.core.api.dto.Brand
import com.procar.core.api.dto.Model
import org.springframework.stereotype.Service

@Service
class CatalogService {

    fun getBrands(query: String?): List<Brand> {
        TODO("Implement brand listing with optional search")
    }

    fun getBrandModels(brandId: String, query: String?): List<Model> {
        TODO("Implement models listing for brand")
    }
}
