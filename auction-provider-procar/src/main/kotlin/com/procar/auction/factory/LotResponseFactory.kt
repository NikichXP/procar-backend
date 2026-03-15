package com.procar.auction.factory

import com.procar.provider.common.PaginationResponse
import com.procar.provider.lot.LotSearchResponse
import org.springframework.stereotype.Component

@Component
class LotResponseFactory {

    fun createEmptySearchResponse(): LotSearchResponse {
        return LotSearchResponse(
            results = emptyList(),
            pagination = PaginationResponse(
                hasNext = false,
                nextCursor = null
            ),
            suggestions = null,
            correctedQuery = null
        )
    }
}
