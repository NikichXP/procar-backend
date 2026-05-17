package com.procar.auction.service

import com.procar.auction.document.BidEntity
import com.procar.auction.document.LotEntity
import com.procar.auction.event.AuctionBuyoutEvent
import com.procar.auction.repository.BidRepository
import com.procar.auction.service.status.LotStatusTransitionService
import com.procar.provider.bid.BidAnalytics
import com.procar.provider.bid.BidAnalyticsResponse
import com.procar.provider.bid.BidStatus
import com.procar.provider.bid.BidTimeAnalytics
import com.procar.provider.bid.BidType
import com.procar.provider.bid.BiddingPattern
import com.procar.provider.bid.PlaceBidRequest
import com.procar.provider.bid.PlaceBidResponse
import com.procar.provider.bid.ProviderBid
import com.procar.provider.bid.ValidateBidRequest
import com.procar.provider.bid.ValidateBidResponse
import com.procar.provider.lot.LotStatus
import com.procar.provider.lot.LotType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Lazy
import org.springframework.data.mongodb.core.FindAndModifyOptions
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
    private val scope: CoroutineScope,
    @Lazy private val lotStatusTransitionService: LotStatusTransitionService
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

        if (!lotStatusTransitionService.lockForPayment(lot.id)) {
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

        val buyoutPrice = lot.buyoutPrice
        val triggersBuyout = buyoutCapEnabled && buyoutPrice != null && request.amount >= buyoutPrice
        val effectiveAmount = if (triggersBuyout) buyoutPrice else request.amount

        if (!bidRepository.advanceBid(lot.id, effectiveAmount, triggersBuyout)) {
            return rejected(request, "Bid rejected: a concurrent bid already met or exceeded your amount", minBid)
        }

        scope.launch {
            // Mark previous winning bids as OUTBID
            mongoTemplate.updateMulti(
                Query(Criteria.where("lot_id").`is`(lot.id).and("status").`is`(BidStatus.WINNING)),
                Update().set("status", BidStatus.OUTBID),
                BidEntity::class.java
            )
        }

        val bid = bidRepository.save(BidEntity(
            lotId = lot.id,
            bidderId = request.bidderId,
            amount = effectiveAmount,
            bidType = if (triggersBuyout) BidType.INSTANT_BUY else request.bidType,
            status = if (triggersBuyout) BidStatus.WON else BidStatus.WINNING,
            isAutoBid = request.bidType == BidType.AUTO
        ))
        bid.isWinning = true

        if (triggersBuyout) {
            eventPublisher.publishEvent(AuctionBuyoutEvent(this, lot.id, request.bidderId, effectiveAmount))
        }

        return PlaceBidResponse(
            bid = bid.toProviderBid(),
            status = bid.status,
            message = if (triggersBuyout) "Buyout via bid accepted" else "Bid placed successfully",
            isWinning = true,
            nextMinimumBid = if (triggersBuyout) 0.0 else effectiveAmount + auction.bidIncrement
        )
    }

    fun getAllBidsForLot(lotId: String): List<BidEntity> {
        val bids = bidRepository.findByLotIdOrderByPlacedAtDesc(lotId)
        val winningAmount = bids
            .filter { it.status == BidStatus.WINNING || it.status == BidStatus.WON }
            .maxOfOrNull { it.amount }
        bids.forEach { bid ->
            bid.isWinning = bid.amount == winningAmount &&
                (bid.status == BidStatus.WINNING || bid.status == BidStatus.WON)
        }
        return bids
    }

    fun getBidsByBidderId(bidderId: String): List<BidEntity> {
        val bids = bidRepository.findByBidderIdOrderByPlacedAtDesc(bidderId)
        // We might want to enrich them with isWinning status relative to the lot
        // But for user history it's usually enough to show the status saved in DB
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
        val buyoutPrice = lot.buyoutPrice
        if (lot.lotType == LotType.HYBRID && buyoutPrice != null && request.amount >= buyoutPrice) {
            warnings.add("Bid meets or exceeds buyout price — this will trigger an instant purchase at $buyoutPrice")
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
            .filter { it.status == BidStatus.WINNING || it.status == BidStatus.OUTBID || it.status == BidStatus.WON }

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
        val auction = lot?.auction
        val lastMinuteBidding = auction != null &&
            bids.any { it.placedAt.isAfter(auction.endTime.minusMinutes(5)) }

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
        val auction = lot?.auction
        if (auction != null) {
            mongoTemplate.updateFirst(
                Query(Criteria.where("_id").`is`(lotId)),
                Update()
                    .set("auction.currentBid", auction.startingBid)
                    .set("auction.totalBids", 0)
                    .set("updatedAt", LocalDateTime.now()),
                LotEntity::class.java
            )
        }
    }

    fun finishAuction(lotId: String) {
        val winningBid = mongoTemplate.findAndModify<BidEntity>(
            Query(Criteria.where("lot_id").`is`(lotId).and("status").`is`(BidStatus.WINNING)),
            Update().set("status", BidStatus.WON),
            FindAndModifyOptions.options().returnNew(true),
            BidEntity::class.java
        )

        if (winningBid != null) {
            // All other bids for this lot that are not WON/OUTBID should probably be LOST
            // But usually we only have WINNING and OUTBID.
            // Let's just make sure everything else is LOST if it's not WON.
            mongoTemplate.updateMulti(
                Query(Criteria.where("lot_id").`is`(lotId).and("status").ne(BidStatus.WON)),
                Update().set("status", BidStatus.LOST),
                BidEntity::class.java
            )
        } else {
            // No bids - auction finished with no winner
            // We can leave it as is, lot status already moved to AWAIT_SELLER_CONFIRMATION or similar
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
