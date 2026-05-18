package com.procar.auction.converter

import com.procar.auction.document.BidEntity
import com.procar.provider.bid.BidStatus
import com.procar.provider.bid.ProviderBid
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class BidDocumentToProviderBidConverter : Converter<BidEntity, ProviderBid> {

    override fun convert(source: BidEntity): ProviderBid {
        val isWinningStatus = source.status == BidStatus.WINNING || source.status == BidStatus.WON
        return ProviderBid(
            id = source.id,
            lotId = source.lotId,
            providerId = source.providerId,
            externalId = null,
            bidderId = source.bidderId,
            amount = source.amount,
            bidType = source.bidType,
            status = source.status,
            isWinning = source.isWinning || isWinningStatus,
            isAutoBid = source.isAutoBid,
            placedAt = source.placedAt
        )
    }
}
