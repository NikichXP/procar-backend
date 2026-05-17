package com.procar.auction.service.status

import com.procar.auction.document.LotEntity
import com.procar.auction.repository.LotRepository
import com.procar.provider.lot.LotStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
abstract class LotStatusUpdateTask(
    protected val lotRepository: LotRepository,
    protected val lotStatusTransitionService: LotStatusTransitionService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    abstract fun fromStatus(): LotStatus
    abstract fun toStatus(): LotStatus
    abstract fun modify(lotEntity: LotEntity): Boolean

    protected fun filter(lotEntity: LotEntity): Boolean = true

    protected fun executeTask() {
        val lots = lotRepository.findByStatus(fromStatus())

        for (lot in lots) {
            if (lotStatusTransitionService.canMigrateToStatus(lot, toStatus()) && filter(lot)) {
                logger.info("Applying task {} to lot {}", this::class.simpleName, lot.id)
                if (modify(lot)) {
                    lotRepository.save(lot)
                }
            }
        }
    }

}