package com.procar.auction.service.status

import com.procar.auction.document.LotEntity
import com.procar.auction.repository.LotRepository
import com.procar.auction.service.InternalAuctionBidService
import com.procar.provider.bid.BidStatus
import com.procar.provider.lot.LotStatus
import com.procar.provider.lot.LotType
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class LotStatusHelper(
    @Lazy private val bidService: InternalAuctionBidService,
    private val lotRepository: LotRepository
) {

    private val statusTestList: List<Pair<LotStatus, (lot: LotEntity) -> Boolean>> = listOf(
        LotStatus.DRAFT to ::isDraftPossible,
        LotStatus.PENDING to ::isPendingPossible,
        LotStatus.ACTIVE to ::isActivePossible,
        LotStatus.AWAITING_PAYMENT to ::isAwaitingPaymentPossible,
        LotStatus.AWAITING_SHIPMENT to ::isAwaitingShipmentPossible,
        LotStatus.IN_TRANSIT to ::isInTransitPossible,
        LotStatus.COMPLETED to ::isCompletedPossible,
        LotStatus.HIDDEN to ::isHiddenPossible
    )

    fun lockForPayment(lotId: String): Boolean = lotRepository.lockForPayment(lotId)

    fun canMigrateToStatus(lotEntity: LotEntity, targetStatus: LotStatus): Boolean {
        return getPossibleStatuses(lotEntity).contains(targetStatus)
    }

    fun getPossibleStatuses(lotEntity: LotEntity): List<LotStatus> {
        val result = mutableListOf<LotStatus>()

        statusTestList.forEach { (status, test) ->
            if (test(lotEntity)) {
                result.add(status)
            }
        }

        return result
    }

    private fun isDraftPossible(lotEntity: LotEntity): Boolean {
        return bidService.getAllBidsForLot(lotEntity.id).isEmpty()
    }

    private fun isPendingPossible(lotEntity: LotEntity): Boolean {
        val hasAuctionMode = lotEntity.lotType in auctionLotTypes
        val hasBuyoutMode = lotEntity.lotType in buyoutLotTypes

        if (hasBuyoutMode) {
            return lotEntity.buyoutPrice != null
        }

        if (hasAuctionMode) {
            return lotEntity.auction?.startingBid != null
        }

        return lotEntity.vehicle.images.isNotEmpty()
    }

    private fun isActivePossible(lotEntity: LotEntity): Boolean {
        return when (lotEntity.lotType) {
            LotType.AUCTION -> isAuctionCanBeActive(lotEntity)
            LotType.BUYOUT -> isPendingPossible(lotEntity)
            LotType.HYBRID -> isAuctionCanBeActive(lotEntity) && isPendingPossible(lotEntity)
        }
    }

    private fun isAwaitingPaymentPossible(lotEntity: LotEntity): Boolean {
        val bids = bidService.getAllBidsForLot(lotEntity.id)
        val hasWonBid = bids.any { it.status == BidStatus.WON }
        if (hasWonBid) return true

        val auction = lotEntity.auction ?: return false
        if (auction.endTime.isBefore(LocalDateTime.now())) {
            return bids.any { it.status == BidStatus.ACCEPTED }
        }
        return false
    }

    private fun isAwaitingShipmentPossible(lotEntity: LotEntity): Boolean = isAwaitingPaymentPossible(lotEntity)

    private fun isInTransitPossible(lotEntity: LotEntity): Boolean = isAwaitingPaymentPossible(lotEntity)

    private fun isCompletedPossible(lotEntity: LotEntity): Boolean = isAwaitingPaymentPossible(lotEntity)

    private fun isHiddenPossible(lotEntity: LotEntity): Boolean {
        return bidService.getAllBidsForLot(lotEntity.id).isEmpty()
    }

    private fun isAuctionCanBeActive(lotEntity: LotEntity): Boolean {
        val now = LocalDateTime.now()
        return lotEntity.auction?.isActiveAt(now) == true
    }

    companion object {
        private val auctionLotTypes = setOf(LotType.AUCTION, LotType.HYBRID)
        private val buyoutLotTypes = setOf(LotType.BUYOUT, LotType.HYBRID)
    }

}