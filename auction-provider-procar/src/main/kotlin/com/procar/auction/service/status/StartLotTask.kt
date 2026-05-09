package com.procar.auction.service.status

import com.procar.auction.document.LotEntity
import com.procar.auction.repository.LotRepository
import com.procar.provider.lot.LotStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class StartLotTask(
    lotRepository: LotRepository,
    lotStatusHelper: LotStatusHelper
) : LotStatusUpdateTask(lotRepository, lotStatusHelper) {

    override fun fromStatus(): LotStatus = LotStatus.PENDING
    override fun toStatus(): LotStatus = LotStatus.ACTIVE

    override fun modify(lotEntity: LotEntity): Boolean {
        lotEntity.status = LotStatus.ACTIVE
        return true
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    fun run() {
        executeTask()
    }
}