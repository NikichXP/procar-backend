package com.procar.auction.service

import com.procar.auction.config.AuctionProperties
import com.procar.auction.document.BidEntity
import com.procar.auction.document.LotEntity
import com.procar.auction.event.AuctionBuyoutEvent
import com.procar.auction.repository.BidRepository
import com.procar.provider.bid.*
import com.procar.provider.lot.LotStatus
import com.procar.provider.lot.LotType
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Lazy
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class InternalAuctionBidService(
    private val bidRepository: BidRepository,
    private val mongoTemplate: MongoTemplate,
    private val eventPublisher: ApplicationEventPublisher,
    @Lazy private val lotStateService: LotStateService
) {

    fun placeBid(request: PlaceBidRequest): PlaceBidResponse {
        val lot = mongoTemplate.findById(request.lotId, LotEntity::class.java)
            ?: return rejected(request, "Lot not found", 0.0)

        if (lot.status != LotStatus.ACTIVE) {
            return rejected(request, "Lot is not active for bidding", 0.0)
        }

        return when (lot.lotType) {
            LotType.BUYOUT -> placeBuyoutOnlyBid(request, lot)
            LotType.AUCTION -> placeAuctionBid(request, lot, buyoutCapEnabled = false)
            LotType.HYBRID -> placeAuctionBid(request, lot, buyoutCapEnabled = true)
        }
    }

    private fun placeBuyoutOnlyBid(request: PlaceBidRequest, lot: LotEntity): PlaceBidResponse {
        val price = lot.buyoutPrice
            ?: return rejected(request, "Lot has no buyout price configured", 0.0)

        if (!lotStateService.lockForPayment(lot.id)) {
            return rejected(request, "Lot is no longer available for purchase", 0.0)
        }

        val bid = bidRepository.save(BidEntity(
            lotId = lot.id,
            bidderId = request.bidderId,
            amount = price,
            bidType = BidType.INSTANT_BUY,
            status = BidStatus.WON,
            isAutoBid = false
        ))
        bid.isWinning = true

        eventPublisher.publishEvent(AuctionBuyoutEvent(this, lot.id, request.bidderId, price))

        return PlaceBidResponse(
            bid = bid.toProviderBid(),
            status = BidStatus.ACCEPTED,
            message = "Purchase accepted",
            isWinning = true,
            nextMinimumBid = 0.0
        )
    }

    private fun placeAuctionBid(request: PlaceBidRequest, lot: LotEntity, buyoutCapEnabled: Boolean): PlaceBidResponse {
        val auction = lot.auction
            ?: return rejected(request, "Lot has no auction configuration", 0.0)

        if (auction.endTime.isBefore(LocalDateTime.now())) {
            return rejected(request, "Auction has ended", 0.0)
        }

        val minBid = auction.currentBid + auction.bidIncrement
        if (request.amount < minBid) {
            return rejected(request, "Bid must be at least $minBid (current: ${auction.currentBid} + step: ${auction.bidIncrement})", minBid)
        }

        val triggersBuyout = buyoutCapEnabled && lot.buyoutPrice != null && request.amount >= lot.buyoutPrice
        val effectiveAmount = if (triggersBuyout) lot.buyoutPrice else request.amount

        if (!bidRepository.advanceBid(lot.id, effectiveAmount, triggersBuyout)) {
            return rejected(request, "Bid rejected: a concurrent bid already met or exceeded your amount", minBid)
        }

        val bid = bidRepository.save(BidEntity(
            lotId = lot.id,
            bidderId = request.bidderId,
            amount = effectiveAmount,
            bidType = if (triggersBuyout) BidType.INSTANT_BUY else request.bidType,
            status = if (triggersBuyout) BidStatus.WON else BidStatus.ACCEPTED,
            isAutoBid = request.bidType == BidType.AUTO
        ))
        bid.isWinning = true

        if (triggersBuyout) {
            eventPublisher.publishEvent(AuctionBuyoutEvent(this, lot.id, request.bidderId, effectiveAmount))
        }

        return PlaceBidResponse(
            bid = bid.toProviderBid(),
            status = BidStatus.ACCEPTED,
            message = if (triggersBuyout) "Buyout via bid accepted" else "Bid placed successfully",
            isWinning = true,
            nextMinimumBid = if (triggersBuyout) 0.0 else effectiveAmount + auction.bidIncrement
        )
    }

    fun getAllBidsForLot(lotId: String): List<BidEntity> {
        val bids = bidRepository.findByLotIdOrderByPlacedAtDesc(lotId)
        val winningAmount = bids
            .filter { it.status == BidStatus.ACCEPTED || it.status == BidStatus.WON }
            .maxOfOrNull { it.amount }
        bids.forEach { bid ->
            bid.isWinning = bid.amount == winningAmount &&
                (bid.status == BidStatus.ACCEPTED || bid.status == BidStatus.WON)
        }
        return bids
    }

    fun validateBid(request: ValidateBidRequest): ValidateBidResponse {
        val lot = mongoTemplate.findById(request.lotId, LotEntity::class.java)
            ?: return ValidateBidResponse(false, "Lot not found", 0.0, null, 0.0, emptyList())

        if (lot.status != LotStatus.ACTIVE) {
            return ValidateBidResponse(false, "Lot is not active", 0.0, null, 0.0, emptyList())
        }

        if (lot.lotType == LotType.BUYOUT) {
            val price = lot.buyoutPrice ?: 0.0
            return ValidateBidResponse(true, "BUYOUT lot: first bid at $price wins", price, price, 0.0, emptyList())
        }

        val auction = lot.auction
            ?: return ValidateBidResponse(false, "Lot has no auction configuration", 0.0, null, 0.0, emptyList())

        if (auction.endTime.isBefore(LocalDateTime.now())) {
            return ValidateBidResponse(false, "Auction has ended", 0.0, null, auction.bidIncrement, emptyList())
        }

        val minBid = auction.currentBid + auction.bidIncrement
        val warnings = mutableListOf<String>()
        if (lot.lotType == LotType.HYBRID && lot.buyoutPrice != null && request.amount >= lot.buyoutPrice) {
            warnings.add("Bid meets or exceeds buyout price — this will trigger an instant purchase at ${lot.buyoutPrice}")
        }

        return ValidateBidResponse(
            isValid = request.amount >= minBid,
            message = if (request.amount < minBid) "Bid must be at least $minBid" else null,
            minimumBid = minBid,
            maximumBid = lot.buyoutPrice,
            bidIncrement = auction.bidIncrement,
            warnings = warnings
        )
    }

    fun getBidAnalytics(lotId: String, timeRange: String?): BidAnalyticsResponse {
        val bids = bidRepository.findByLotIdOrderByPlacedAtDesc(lotId)
            .filter { it.status == BidStatus.ACCEPTED || it.status == BidStatus.WON }

        if (bids.isEmpty()) {
            return BidAnalyticsResponse(lotId = lotId, isSupported = false, analytics = null, message = "No bids found for analytics")
        }

        val sorted = bids.sortedBy { it.placedAt }
        val firstBidTime = sorted.first().placedAt
        val lastBidTime = sorted.last().placedAt
        val uniqueBidders = bids.map { it.bidderId }.distinct().size
        val durationHours = java.time.Duration.between(firstBidTime, lastBidTime).toSeconds() / 3600.0
        val bidFrequency = bids.size / if (durationHours > 0) durationHours else 1.0
        val increments = sorted.zipWithNext().map { (a, b) -> b.amount - a.amount }
        val avgIncrement = if (increments.isNotEmpty()) increments.average() else 0.0
        val bidsByHour = bids.groupBy { it.placedAt.hour }.mapValues { it.value.size }
        val avgTimeBetweenBids = if (bids.size > 1)
            sorted.zipWithNext().map { (a, b) -> java.time.Duration.between(a.placedAt, b.placedAt).toMinutes().toDouble() }.average()
        else 0.0
        val lot = mongoTemplate.findById(lotId, LotEntity::class.java)
        val lastMinuteBidding = lot?.auction != null &&
            bids.any { it.placedAt.isAfter(lot.auction.endTime.minusMinutes(5)) }

        return BidAnalyticsResponse(
            lotId = lotId,
            isSupported = true,
            analytics = BidAnalytics(
                totalBids = bids.size,
                uniqueBidders = uniqueBidders,
                bidFrequency = bidFrequency,
                averageBidIncrement = avgIncrement,
                biddingPattern = BiddingPattern(
                    mostActiveHour = bidsByHour.maxByOrNull { it.value }?.key ?: 0,
                    peakBiddingPeriod = "Evening",
                    averageTimeBetweenBids = avgTimeBetweenBids,
                    lastMinuteBidding = lastMinuteBidding
                ),
                timeAnalytics = BidTimeAnalytics(
                    firstBidTime = firstBidTime,
                    lastBidTime = lastBidTime,
                    biddingDuration = java.time.Duration.between(firstBidTime, lastBidTime).toSeconds(),
                    bidsByHour = bidsByHour
                )
            ),
            message = null
        )
    }

    fun deleteAllBidsForLot(lotId: String) {
        bidRepository.deleteByLotId(lotId)
        val lot = mongoTemplate.findById(lotId, LotEntity::class.java)
        if (lot?.auction != null) {
            mongoTemplate.updateFirst(
                Query(Criteria.where("_id").`is`(lotId)),
                Update()
                    .set("auction.currentBid", lot.auction.startingBid)
                    .set("auction.totalBids", 0)
                    .set("updatedAt", LocalDateTime.now()),
                LotEntity::class.java
            )
        }
    }

    private fun rejected(request: PlaceBidRequest, message: String, nextMinimumBid: Double) = PlaceBidResponse(
        bid = ProviderBid(
            id = "error-${System.currentTimeMillis()}",
            lotId = request.lotId,
            providerId = "procar",
            externalId = null,
            bidderId = request.bidderId,
            amount = request.amount,
            bidType = request.bidType,
            status = BidStatus.REJECTED,
            isWinning = false,
            isAutoBid = request.bidType == BidType.AUTO,
            placedAt = LocalDateTime.now()
        ),
        status = BidStatus.REJECTED,
        message = message,
        isWinning = false,
        nextMinimumBid = nextMinimumBid
    )
}

private fun BidEntity.toProviderBid() = ProviderBid(
    id = id,
    lotId = lotId,
    providerId = providerId,
    externalId = null,
    bidderId = bidderId,
    amount = amount,
    bidType = bidType,
    status = status,
    isWinning = isWinning,
    isAutoBid = isAutoBid,
    placedAt = placedAt
)
