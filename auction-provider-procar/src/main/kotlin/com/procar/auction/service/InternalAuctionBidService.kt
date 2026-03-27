package com.procar.auction.service

import com.procar.auction.config.AuctionProperties
import com.procar.auction.document.BidDocument
import com.procar.auction.repository.BidRepository
import com.procar.auction.repository.LotRepository
import com.procar.auction.util.BidsFactory
import com.procar.provider.bid.*
import com.procar.provider.lot.LotStatus
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class InternalAuctionBidService(
    private val bidRepository: BidRepository,
    private val lotRepository: LotRepository,
    private val auctionProperties: AuctionProperties,
    private val conversionService: ConversionService
) {

    fun placeBid(request: PlaceBidRequest): PlaceBidResponse {
        val lot = lotRepository.findById(request.lotId).orElse(null)
            ?: return PlaceBidResponse(
                bid = createErrorBid(request),
                status = BidStatus.REJECTED,
                message = "Lot not found",
                isWinning = false,
                nextMinimumBid = 0.0
            )

        if (lot.status != LotStatus.ACTIVE) {
            return PlaceBidResponse(
                bid = createErrorBid(request),
                status = BidStatus.REJECTED,
                message = "Lot is not active for bidding",
                isWinning = false,
                nextMinimumBid = calculateNextMinimumBid(lot.auction.currentBid)
            )
        }

        if (lot.auction.endTime.isBefore(LocalDateTime.now())) {
            return PlaceBidResponse(
                bid = createErrorBid(request),
                status = BidStatus.REJECTED,
                message = "Auction has ended",
                isWinning = false,
                nextMinimumBid = calculateNextMinimumBid(lot.auction.currentBid)
            )
        }

        val validation = validateBid(request.lotId, request.bidderId, request.amount)
        if (!validation.isValid) {
            return PlaceBidResponse(
                bid = createErrorBid(request),
                status = BidStatus.REJECTED,
                message = validation.message,
                isWinning = false,
                nextMinimumBid = validation.minimumBid
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

        // Create and save the bid
        val bidDocument = BidDocument(
            lotId = request.lotId,
            externalId = UUID.randomUUID().toString(),
            bidderId = request.bidderId,
            amount = request.amount,
            bidType = request.bidType,
            status = BidStatus.ACCEPTED,
            isWinning = true, // Will be updated after checking other bids
            isAutoBid = request.bidType == BidType.AUTO
        )

        val savedBid = bidRepository.save(bidDocument)

        // Update other bids' winning status
        updateWinningBids(request.lotId, savedBid.amount)

        // Update lot's current bid and total bids
        val updatedLot = lot.copy(
            auction = lot.auction.copy(
                currentBid = request.amount,
                totalBids = lot.auction.totalBids + 1
            ),
            updatedAt = LocalDateTime.now()
        )
        lotRepository.save(updatedLot)

        return PlaceBidResponse(
            bid = conversionService.convert(savedBid, ProviderBid::class.java)!!,
            status = BidStatus.ACCEPTED,
            message = "Bid placed successfully",
            isWinning = true,
            nextMinimumBid = calculateNextMinimumBid(request.amount)
        )
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

        if (lot.status != LotStatus.ACTIVE) {
            return ValidateBidResponse(
                isValid = false,
                message = "Lot is not active for bidding",
                minimumBid = calculateNextMinimumBid(lot.auction.currentBid),
                maximumBid = null,
                bidIncrement = auctionProperties.bid.defaultIncrement,
                warnings = emptyList()
            )
        }

        if (lot.auction.endTime.isBefore(LocalDateTime.now())) {
            return ValidateBidResponse(
                isValid = false,
                message = "Auction has ended",
                minimumBid = calculateNextMinimumBid(lot.auction.currentBid),
                maximumBid = null,
                bidIncrement = auctionProperties.bid.defaultIncrement,
                warnings = emptyList()
            )
        }

        val minimumBid = calculateNextMinimumBid(lot.auction.currentBid)
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

        if (lot.auction.reservePrice != null && amount < lot.auction.reservePrice) {
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
                    lot != null && it.placedAt.isAfter(lot.auction.endTime.minusMinutes(5))
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
        if (lot != null) {
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
