package com.procar.gateway.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class Brand(
    @ApiDoc(example = "01956b0a-aaaa-7000-8000-000000000001") val id: String,
    @ApiDoc(example = "Toyota") val name: String,
    @ApiDoc(example = "https://cdn.procar.com/logos/toyota.png") val logoUrl: String? = null,
)

@Serializable
data class Model(
    @ApiDoc(example = "01956b0a-bbbb-7000-8000-000000000002") val id: String,
    @ApiDoc(example = "Camry") val name: String,
    @ApiDoc(example = "01956b0a-aaaa-7000-8000-000000000001") val brandId: String,
)
