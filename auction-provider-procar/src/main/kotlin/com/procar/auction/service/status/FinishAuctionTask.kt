package com.procar.auction.service.status

import com.procar.auction.document.LotEntity
import com.procar.auction.repository.LotRepository
import com.procar.provider.lot.LotStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class FinishAuctionTask(
    lotRepository: LotRepository,
    lotStatusTransitionService: LotStatusTransitionService
) : LotStatusUpdateTask(lotRepository, lotStatusTransitionService) {

    override fun fromStatus(): LotStatus = LotStatus.ACTIVE
    override fun toStatus(): LotStatus = LotStatus.AWAIT_SELLER_CONFIRMATION

    override fun modify(lotEntity: LotEntity): Boolean {
        lotEntity.status = LotStatus.AWAIT_SELLER_CONFIRMATION
        return true
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    fun run() {
        executeTask()
    }
}