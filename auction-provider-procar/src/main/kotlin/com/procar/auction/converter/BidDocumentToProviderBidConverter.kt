package com.procar.auction.converter

import com.procar.auction.document.BidDocument
import com.procar.provider.bid.ProviderBid
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class BidDocumentToProviderBidConverter : Converter<BidDocument, ProviderBid> {

    override fun convert(source: BidDocument): ProviderBid {
        return ProviderBid(
            id = source.id,
            lotId = source.lotId,
            providerId = source.providerId,
            externalId = source.externalId,
            bidderId = source.bidderId,
            amount = source.amount,
            bidType = source.bidType,
            status = source.status,
            isWinning = source.isWinning,
            isAutoBid = source.isAutoBid,
            placedAt = source.placedAt
        )
    }
}
