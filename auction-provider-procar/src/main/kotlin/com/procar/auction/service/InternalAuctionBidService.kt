package com.procar.auction.service

import com.procar.auction.config.AuctionProperties
import com.procar.auction.document.BidDocument
import com.procar.auction.document.LotDocument
import com.procar.auction.repository.BidRepository
import com.procar.auction.repository.LotRepository
import com.procar.auction.util.BidsFactory
import com.procar.provider.bid.*
import com.procar.provider.lot.LotStatus
import org.springframework.core.convert.ConversionService
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

// TODO This service needs to be refactored - it's right now vibecoded
//  idea for future: update if winning condition is met
//  update set winning bid + winner id IF ( current bid + bid increment ) >= new bid
@Service
class InternalAuctionBidService(
    private val bidRepository: BidRepository,
    private val lotRepository: LotRepository,
    private val auctionProperties: AuctionProperties,
    private val conversionService: ConversionService,
    private val mongoTemplate: MongoTemplate
) {

    fun placeBid(request: PlaceBidRequest): PlaceBidResponse {
        // First check lot type to reject BUYOUT lots early
        val lot = lotRepository.findById(request.lotId).orElse(null)
        if (lot?.lotType == com.procar.provider.lot.LotType.BUYOUT) {
            return PlaceBidResponse(
                bid = createErrorBid(request),
                status = BidStatus.REJECTED,
                message = "Bidding not allowed on buyout-only lot",
                isWinning = false,
                nextMinimumBid = 0.0,
            )
        }

        val query = Query.query(
            Criteria.where("_id").isEqualTo(request.lotId)
                .and("status").isEqualTo(LotStatus.ACTIVE)
                .and("auction.endTime").gt(LocalDateTime.now())
        )
        
        val minimumBid = calculateNextMinimumBidForLot(request.lotId)
        
        if (request.amount < minimumBid) {
            return PlaceBidResponse(
                bid = createErrorBid(request),
                status = BidStatus.REJECTED,
                message = "Bid amount is below minimum required bid",
                isWinning = false,
                nextMinimumBid = minimumBid
            )
        }
        
        // Check if bidder already has a higher bid
        val existingHigherBid = bidRepository.findByLotIdAndBidderIdAndStatus(
            request.lotId, 
            request.bidderId, 
            BidStatus.ACCEPTED
        ).maxByOrNull { it.amount }

        if (existingHigherBid != null && existingHigherBid.amount >= request.amount) {
            return PlaceBidResponse(
                bid = createErrorBid(request),
                status = BidStatus.REJECTED,
                message = "You already have a higher bid",
                isWinning = false,
                nextMinimumBid = calculateNextMinimumBid(existingHigherBid.amount)
            )
        }

        // Compute cap for HYBRID lots
        val capped = lot?.lotType == com.procar.provider.lot.LotType.HYBRID &&
            lot.buyoutPrice != null &&
            request.amount >= lot.buyoutPrice
        val effectiveAmount = if (capped) lot.buyoutPrice!! else request.amount

        // Atomic update of lot's current bid and total bids
        val update = Update()
            .inc("auction.totalBids", 1)
            .set("auction.currentBid", effectiveAmount)
            .set("updatedAt", LocalDateTime.now())

        val updatedLot: LotDocument? = mongoTemplate.findAndModify(query, update, LotDocument::class.java)
        
        if (updatedLot == null) {
            // Lot not found or not in valid state (inactive/ended)
            val lot = lotRepository.findById(request.lotId).orElse(null)
            return when {
                lot == null -> PlaceBidResponse(
                    bid = createErrorBid(request),
                    status = BidStatus.REJECTED,
                    message = "Lot not found",
                    isWinning = false,
                    nextMinimumBid = 0.0
                )
                lot.status != LotStatus.ACTIVE -> PlaceBidResponse(
                    bid = createErrorBid(request),
                    status = BidStatus.REJECTED,
                    message = "Lot is not active for bidding",
                    isWinning = false,
                    nextMinimumBid = lot.auction?.let { calculateNextMinimumBid(it.currentBid) } ?: 0.0
                )
                lot.auction?.endTime?.isBefore(LocalDateTime.now()) == true -> PlaceBidResponse(
                    bid = createErrorBid(request),
                    status = BidStatus.REJECTED,
                    message = "Auction has ended",
                    isWinning = false,
                    nextMinimumBid = lot.auction?.let { calculateNextMinimumBid(it.currentBid) } ?: 0.0
                )
                else -> PlaceBidResponse(
                    bid = createErrorBid(request),
                    status = BidStatus.REJECTED,
                    message = "Bid validation failed",
                    isWinning = false,
                    nextMinimumBid = lot.auction?.let { calculateNextMinimumBid(it.currentBid) } ?: 0.0
                )
            }
        }

        // Create and save the bid
        val bidDocument = BidDocument(
            lotId = request.lotId,
            externalId = UUID.randomUUID().toString(),
            bidderId = request.bidderId,
            amount = effectiveAmount,
            bidType = if (capped) BidType.INSTANT_BUY else request.bidType,
            status = if (capped) BidStatus.WON else BidStatus.ACCEPTED,
            isWinning = true,
            isAutoBid = request.bidType == BidType.AUTO
        )

        val savedBid = bidRepository.save(bidDocument)

        // If capped, set lot status to AWAITING_PAYMENT
        if (capped && updatedLot != null) {
            val lotToUpdate = updatedLot.copy(status = LotStatus.AWAITING_PAYMENT)
            lotRepository.save(lotToUpdate)
        }

        // Update other bids' winning status
        updateWinningBids(request.lotId, savedBid.amount)

        return if (capped) {
            PlaceBidResponse(
                bid = conversionService.convert(savedBid, ProviderBid::class.java)!!,
                status = BidStatus.ACCEPTED,
                message = "Buyout via bid accepted",
                isWinning = true,
                nextMinimumBid = 0.0
            )
        } else {
            PlaceBidResponse(
                bid = conversionService.convert(savedBid, ProviderBid::class.java)!!,
                status = BidStatus.ACCEPTED,
                message = "Bid placed successfully",
                isWinning = true,
                nextMinimumBid = calculateNextMinimumBid(request.amount)
            )
        }
    }

    fun validateBid(request: ValidateBidRequest): ValidateBidResponse {
        return validateBid(request.lotId, request.bidderId, request.amount)
    }

    private fun validateBid(lotId: String, bidderId: String, amount: Double): ValidateBidResponse {
        val lot = lotRepository.findById(lotId).orElse(null)
            ?: return ValidateBidResponse(
                isValid = false,
                message = "Lot not found",
                minimumBid = 0.0,
                maximumBid = null,
                bidIncrement = auctionProperties.bid.defaultIncrement,
                warnings = emptyList()
            )

        if (lot.lotType == com.procar.provider.lot.LotType.BUYOUT) {
            return ValidateBidResponse(
                isValid = false,
                message = "Bidding not allowed on buyout-only lot",
                minimumBid = 0.0, maximumBid = null,
                bidIncrement = auctionProperties.bid.defaultIncrement,
                warnings = emptyList(),
            )
        }

        if (lot.status != LotStatus.ACTIVE) {
            return ValidateBidResponse(
                isValid = false,
                message = "Lot is not active for bidding",
                minimumBid = lot.auction?.let { calculateNextMinimumBid(it.currentBid) } ?: 0.0,
                maximumBid = null,
                bidIncrement = auctionProperties.bid.defaultIncrement,
                warnings = emptyList()
            )
        }

        if (lot.auction?.endTime?.isBefore(LocalDateTime.now()) == true) {
            return ValidateBidResponse(
                isValid = false,
                message = "Auction has ended",
                minimumBid = lot.auction?.let { calculateNextMinimumBid(it.currentBid) } ?: 0.0,
                maximumBid = null,
                bidIncrement = auctionProperties.bid.defaultIncrement,
                warnings = emptyList()
            )
        }

        val minimumBid = lot.auction?.let { calculateNextMinimumBid(it.currentBid) } ?: 0.0
        val warnings = mutableListOf<String>()

        if (amount < minimumBid) {
            return ValidateBidResponse(
                isValid = false,
                message = "Bid amount is below minimum required bid",
                minimumBid = minimumBid,
                maximumBid = null,
                bidIncrement = auctionProperties.bid.defaultIncrement,
                warnings = warnings
            )
        }

        if (lot.auction?.reservePrice != null && amount < lot.auction.reservePrice) {
            warnings.add("Bid is below reserve price")
        }

        if (amount > auctionProperties.bid.maxAutoBidAmount) {
            warnings.add("Bid amount exceeds maximum auto-bid limit")
        }

        return ValidateBidResponse(
            isValid = true,
            message = "Bid is valid",
            minimumBid = minimumBid,
            maximumBid = auctionProperties.bid.maxAutoBidAmount,
            bidIncrement = auctionProperties.bid.defaultIncrement,
            warnings = warnings
        )
    }

    fun getBidHistory(lotId: String, connectionId: String?): BidHistoryResponse {
        val bids = bidRepository.findByLotIdOrderByPlacedAtDesc(lotId)
            .mapNotNull { conversionService.convert(it, ProviderBid::class.java) }

        val summary = calculateBidSummary(bids)

        return BidHistoryResponse(
            lotId = lotId,
            bids = bids,
            pagination = com.procar.provider.common.PaginationResponse(
                hasNext = false,
                nextCursor = null
            ),
            summary = summary
        )
    }

    fun getBidAnalytics(lotId: String, timeRange: String?): BidAnalyticsResponse {
        val bids = bidRepository.findAcceptedBidsByLotId(lotId)
        
        if (bids.isEmpty()) {
            return BidAnalyticsResponse(
                lotId = lotId,
                isSupported = false,
                analytics = null,
                message = "No bids found for analytics"
            )
        }

        val analytics = calculateBidAnalytics(bids)

        return BidAnalyticsResponse(
            lotId = lotId,
            isSupported = true,
            analytics = analytics,
            message = null
        )
    }

    private fun updateWinningBids(lotId: String, newAmount: Double) {
        val allBids = bidRepository.findByLotId(lotId)
        val highestBid = allBids.maxByOrNull { it.amount }

        allBids.forEach { bid ->
            bid.isWinning = bid.amount == highestBid?.amount && bid.status == BidStatus.ACCEPTED
            bidRepository.save(bid)
        }
    }

    private fun calculateNextMinimumBid(currentBid: Double): Double {
        val increment = when {
            currentBid < 1000 -> auctionProperties.bid.minimumIncrement
            currentBid < 10000 -> auctionProperties.bid.defaultIncrement
            else -> auctionProperties.bid.maximumIncrement
        }
        return currentBid + increment
    }

    private fun calculateNextMinimumBidForLot(lotId: String): Double {
        val lot = lotRepository.findById(lotId).orElse(null)
            ?: return 0.0
        return lot.auction?.let { calculateNextMinimumBid(it.currentBid) } ?: 0.0
    }

    private fun calculateBidSummary(bids: List<ProviderBid>): BidSummary {
        if (bids.isEmpty()) {
            return BidSummary(
                totalBids = 0,
                currentBid = 0.0,
                bidCount = 0,
                highestBid = 0.0,
                lowestBid = 0.0,
                averageBid = 0.0
            )
        }

        val acceptedBids = bids.filter { it.status == BidStatus.ACCEPTED }
        return BidSummary(
            totalBids = bids.size,
            currentBid = acceptedBids.maxOfOrNull { it.amount } ?: 0.0,
            bidCount = acceptedBids.size,
            highestBid = acceptedBids.maxOfOrNull { it.amount } ?: 0.0,
            lowestBid = acceptedBids.minOfOrNull { it.amount } ?: 0.0,
            averageBid = if (acceptedBids.isNotEmpty()) acceptedBids.map { it.amount }.average() else 0.0
        )
    }

    private fun calculateBidAnalytics(bids: List<BidDocument>): BidAnalytics {
        val uniqueBidders = bids.map { it.bidderId }.distinct().size
        val bidFrequency = bids.size.toDouble() / 
            (if (bids.isNotEmpty()) {
                val duration = (bids.last().placedAt.toEpochSecond(java.time.ZoneOffset.UTC) - bids.first().placedAt.toEpochSecond(java.time.ZoneOffset.UTC)) / 3600.0
                if (duration > 0) duration else 1.0
            } else 1.0)

        val bidIncrements = bids.sortedBy { it.placedAt }.zipWithNext().mapNotNull { (prev, curr) ->
            if (prev.placedAt.isBefore(curr.placedAt)) curr.amount - prev.amount else null
        }
        val averageBidIncrement = if (bidIncrements.isNotEmpty()) bidIncrements.average() else 0.0

        val bidsByHour = bids.groupBy { it.placedAt.hour }.mapValues { it.value.size }

        return BidAnalytics(
            totalBids = bids.size,
            uniqueBidders = uniqueBidders,
            bidFrequency = bidFrequency,
            averageBidIncrement = averageBidIncrement,
            biddingPattern = BiddingPattern(
                mostActiveHour = bidsByHour.maxByOrNull { it.value }?.key ?: 0,
                peakBiddingPeriod = "Evening",
                averageTimeBetweenBids = if (bids.size > 1) {
                    bids.sortedBy { it.placedAt }.zipWithNext().map { (prev, curr) ->
                        java.time.Duration.between(prev.placedAt, curr.placedAt).toMinutes().toDouble()
                    }.average()
                } else 0.0,
                lastMinuteBidding = bids.any { 
                    val lot = lotRepository.findById(it.lotId).orElse(null)
                    lot != null && lot.auction != null && it.placedAt.isAfter(lot.auction.endTime.minusMinutes(5))
                }
            ),
            timeAnalytics = BidTimeAnalytics(
                firstBidTime = bids.minOfOrNull { it.placedAt } ?: LocalDateTime.now(),
                lastBidTime = bids.maxOfOrNull { it.placedAt } ?: LocalDateTime.now(),
                biddingDuration = if (bids.size > 1) {
                    java.time.Duration.between(bids.minOf { it.placedAt }, bids.maxOf { it.placedAt }).toSeconds()
                } else 0L,
                bidsByHour = bidsByHour
            )
        )
    }

    private fun createErrorBid(request: PlaceBidRequest): ProviderBid {
        return BidsFactory.createErrorBid(request)
    }

    fun deleteAllBidsForLot(lotId: String) {
        bidRepository.deleteByLotId(lotId)
        
        // Reset lot's auction information
        val lot = lotRepository.findById(lotId).orElse(null)
        if (lot != null && lot.auction != null) {
            val updatedLot = lot.copy(
                auction = lot.auction.copy(
                    currentBid = lot.auction.startingBid,
                    totalBids = 0
                ),
                updatedAt = LocalDateTime.now()
            )
            lotRepository.save(updatedLot)
        }
    }
}
