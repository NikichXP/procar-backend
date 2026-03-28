package com.procar.provider.lot

import com.procar.provider.common.PaginationResponse

data class LotSearchResponse(
    val results: List<VehicleLot>,
    val pagination: PaginationResponse,
    val suggestions: List<String>?,
    val correctedQuery: String?
)
