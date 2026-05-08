package com.procar.auction.service.status

import com.procar.auction.document.LotEntity
import com.procar.auction.repository.LotRepository
import com.procar.auction.service.InternalAuctionLotService
import com.procar.provider.lot.LotStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class LotStatusUpdateTaskScheduler(
    private val lotRepository: LotRepository,
    private val internalAuctionLotService: InternalAuctionLotService,
    private val lotStatusHelper: LotStatusHelper
) {

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    fun run() {
        lotRepository.findByStatus(LotStatus.PENDING).forEach {
// todo
        }
    }

    private fun checkAndStartPending(lot: LotEntity) {
        if (lotStatusHelper.canMigrateToStatus(lot, LotStatus.ACTIVE)) {
// todo
        }
    }

}