package com.procar.core.api.dto

data class LotSearchRequest(
    val status: LotStatus?,
    val source: String?,
    val endsInMinutes: Int?,
    val brandId: String?,
    val modelId: String?,
    val yearFrom: Int?,
    val yearTo: Int?,
    val priceFrom: Double?,
    val priceTo: Double?,
    val condition: CarCondition?,
    val offset: Int = 0,
    val limit: Int = 20,
    val sort: String = "endTime,asc"
)
