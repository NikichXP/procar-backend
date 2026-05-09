package com.procar.auction.service.status

import com.procar.auction.document.LotEntity
import com.procar.provider.lot.LotStatus
import org.springframework.stereotype.Component

@Component
class StartLotTask : LotStatusUpdateTask {

    override fun fromStatus(): LotStatus = LotStatus.PENDING
    override fun toStatus(): LotStatus = LotStatus.ACTIVE

    override fun modify(lotEntity: LotEntity): Boolean {
        lotEntity.status = LotStatus.ACTIVE
        return true
    }
}