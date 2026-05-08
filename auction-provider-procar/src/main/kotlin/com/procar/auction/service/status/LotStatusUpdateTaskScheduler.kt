package com.procar.auction.service.status

import com.procar.auction.document.LotEntity
import com.procar.auction.repository.LotRepository
import com.procar.auction.service.InternalAuctionLotService
import com.procar.provider.lot.LotStatus
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class LotStatusUpdateTaskScheduler(
    private val lotRepository: LotRepository,
    private val internalAuctionLotService: InternalAuctionLotService,
    private val lotStatusHelper: LotStatusHelper,
    private val lotStatusUpdateTask: List<LotStatusUpdateTask>
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    fun run() {
        val statusesToCheck = lotStatusUpdateTask.map { it.fromStatus() }.distinct()

        for (status in statusesToCheck) {
            val lots = lotRepository.findByStatus(status)

            for (lot in lots) {
                val possibleTargetStatuses = lotStatusHelper.getPossibleStatuses(lot)

                val task = lotStatusUpdateTask
                    .find { it.fromStatus() == status && it.toStatus() in possibleTargetStatuses }

                if (task != null) {
                    applyTask(task, lot)
                }
            }

        }
    }

    private fun applyTask(task: LotStatusUpdateTask, lot: LotEntity) {
        logger.info("Applying task {} to lot {}", task, lot)
        task.execute(lot)
        lotRepository.save(lot)
    }

}