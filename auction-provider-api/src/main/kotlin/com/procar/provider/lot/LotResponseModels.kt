package com.procar.provider.lot

import com.procar.provider.common.*

data class LotSearchResponse(
    val results: List<ProviderLot>,
    val pagination: PaginationResponse,
    val suggestions: List<String>?,
    val correctedQuery: String?
)
