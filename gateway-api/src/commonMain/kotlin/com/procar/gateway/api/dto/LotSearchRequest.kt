package com.procar.gateway.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class LotSearchRequest(
    val statuses: List<LotStatus>,
    val source: String?,
    val endsInMinutes: Int?,
    val brandId: String?,
    val modelId: String?,
    val yearFrom: Int?,
    val yearTo: Int?,
    val priceFrom: Double?,
    val priceTo: Double?,
    val condition: CarCondition?,
    val cursor: String? = null,
    val limit: Int = 20,
    val sort: String = "endTime,asc",
)
