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
class LotStatusTransitionService(
    @Lazy private val bidService: InternalAuctionBidService,
    private val lotRepository: LotRepository
) {

    private val allowedTransitions = mapOf(
        LotStatus.DRAFT to setOf(LotStatus.PENDING, LotStatus.HIDDEN),
        LotStatus.PENDING to setOf(LotStatus.ACTIVE, LotStatus.DRAFT, LotStatus.HIDDEN),
        LotStatus.ACTIVE to setOf(LotStatus.PENDING, LotStatus.AWAIT_SELLER_CONFIRMATION, LotStatus.HIDDEN),
        LotStatus.AWAIT_SELLER_CONFIRMATION to setOf(LotStatus.AWAITING_PAYMENT, LotStatus.PENDING, LotStatus.HIDDEN),
        LotStatus.AWAITING_PAYMENT to setOf(LotStatus.AWAITING_SHIPMENT, LotStatus.PENDING, LotStatus.HIDDEN),
        LotStatus.AWAITING_SHIPMENT to setOf(LotStatus.IN_TRANSIT, LotStatus.HIDDEN),
        LotStatus.IN_TRANSIT to setOf(LotStatus.COMPLETED, LotStatus.HIDDEN),
        LotStatus.COMPLETED to setOf(LotStatus.HIDDEN),
        LotStatus.HIDDEN to setOf(LotStatus.DRAFT, LotStatus.PENDING)
    )

    private val statusValidators: Map<LotStatus, (lot: LotEntity) -> Boolean> = mapOf(
        LotStatus.DRAFT to ::isDraftPossible,
        LotStatus.PENDING to ::isPendingPossible,
        LotStatus.ACTIVE to ::isActivePossible,
        LotStatus.AWAIT_SELLER_CONFIRMATION to ::isAwaitSellerConfirmationPossible,
        LotStatus.AWAITING_PAYMENT to ::isAwaitingPaymentPossible,
        LotStatus.AWAITING_SHIPMENT to ::isAwaitingShipmentPossible,
        LotStatus.IN_TRANSIT to ::isInTransitPossible,
        LotStatus.COMPLETED to ::isCompletedPossible,
        LotStatus.HIDDEN to ::isHiddenPossible
    )

    fun lockForPayment(lotId: String): Boolean = lotRepository.lockForPayment(lotId)

    fun canMigrateToStatus(lotEntity: LotEntity, targetStatus: LotStatus): Boolean {
        // 1. Check if transition is allowed from current status
        val possibleTransitions = allowedTransitions[lotEntity.status] ?: emptySet()
        if (targetStatus !in possibleTransitions) return false

        // 2. Check if target status is possible for this specific lot (data requirements)
        return statusValidators[targetStatus]?.invoke(lotEntity) ?: false
    }

    fun getPossibleStatuses(lotEntity: LotEntity): List<LotStatus> {
        return LotStatus.entries.filter { canMigrateToStatus(lotEntity, it) }
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
        if (lotEntity.status == LotStatus.AWAIT_SELLER_CONFIRMATION) return false
        return when (lotEntity.lotType) {
            LotType.AUCTION -> isAuctionCanBeActive(lotEntity)
            LotType.BUYOUT -> isPendingPossible(lotEntity)
            LotType.HYBRID -> isAuctionCanBeActive(lotEntity) && isPendingPossible(lotEntity)
        }
    }

    private fun isAwaitSellerConfirmationPossible(lotEntity: LotEntity): Boolean {
        // Can move to confirmation if auction ended or buyout requested
        return true // Logic can be more complex based on bids
    }

    private fun isAwaitingPaymentPossible(lotEntity: LotEntity): Boolean {
        if (lotEntity.status == LotStatus.AWAIT_SELLER_CONFIRMATION) return true
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
        return true // Can always hide/archive
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