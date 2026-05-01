package com.procar.gateway.api.dto

import io.swagger.v3.oas.annotations.media.Schema

data class Brand(
    @Schema(example = "01956b0a-aaaa-7000-8000-000000000001") val id: String,
    @Schema(example = "Toyota") val name: String,
    @Schema(example = "https://cdn.procar.com/logos/toyota.png") val logoUrl: String? = null
)

data class Model(
    @Schema(example = "01956b0a-bbbb-7000-8000-000000000002") val id: String,
    @Schema(example = "Camry") val name: String,
    @Schema(example = "01956b0a-aaaa-7000-8000-000000000001") val brandId: String
)
