package com.procar.auction.service.status

import com.procar.auction.document.LotEntity
import com.procar.auction.service.InternalAuctionBidService
import com.procar.provider.lot.LotStatus
import org.springframework.stereotype.Component

@Component
class FinishAuctionTask(
    private val bidService: InternalAuctionBidService
) : LotStatusUpdateTask {

    override fun fromStatus(): LotStatus = LotStatus.ACTIVE
    override fun toStatus(): LotStatus = LotStatus.AWAITING_PAYMENT

    override fun modify(lotEntity: LotEntity): Boolean {
        lotEntity.status = LotStatus.AWAITING_PAYMENT
        return true
    }
}