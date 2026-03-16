package com.procar.core.service

import com.procar.core.api.dto.LotDetail
import com.procar.core.api.dto.LotSummary
import com.procar.core.api.dto.LotSearchRequest
import org.springframework.stereotype.Service

@Service
class LotService {

    fun getLots(request: LotSearchRequest): List<LotSummary> {
        TODO("Implement lots listing with filters - return raw list")
    }

    fun countLots(request: LotSearchRequest): Int {
        TODO("Implement count of lots matching filters")
    }

    fun getLotDetail(lotId: String): LotDetail {
        TODO("Implement lot detail retrieval")
    }
}
