package com.procar.core.service

import com.procar.core.api.dto.Bid
import com.procar.core.api.dto.BidPage
import com.procar.core.api.dto.BidRequest
import org.springframework.stereotype.Service

@Service
class BidService {

    fun getBidHistory(lotId: String, page: Int, size: Int): BidPage {
        TODO("Implement bid history retrieval")
    }

    fun placeBid(lotId: String, bidRequest: BidRequest): Bid {
        TODO("Implement bid placement")
    }
}
