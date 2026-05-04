package com.procar.auction.service

import com.procar.auction.document.BidEntity
import com.procar.auction.document.LotEntity
import com.procar.auction.repository.BidRepository
import com.procar.provider.bid.*
import com.procar.provider.lot.LotStatus
import com.procar.provider.lot.LotType
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import java.time.LocalDateTime
import kotlin.test.assertEquals

class BuyoutServiceTest {

    private val mongo: MongoTemplate = org.mockito.kotlin.mock()
    private val bidRepository: BidRepository = org.mockito.kotlin.mock()

    private val buyoutService = BuyoutService(mongo, bidRepository)

    @Test
    fun `buyout should accept BUYOUT lot`() {
        // Given
        val lotId = "lot-123"
        val bidderId = "user-456"
        val lot = org.mockito.kotlin.mock<LotEntity> {
            on { id }.thenReturn(lotId)
            on { status }.thenReturn(LotStatus.ACTIVE)
            on { lotType }.thenReturn(LotType.BUYOUT)
            on { buyoutPrice }.thenReturn(25000.0)
        }

        whenever(mongo.findAndModify(
            any<Query>(),
            any<Update>(),
            any<FindAndModifyOptions>(),
            eq(LotEntity::class.java)
        )).thenReturn(lot)

        whenever(bidRepository.save(any<BidEntity>())).thenReturn(
            BidEntity(
                lotId = lotId,
                bidderId = bidderId,
                amount = 25000.0,
                bidType = BidType.INSTANT_BUY,
                status = BidStatus.WON,
                isAutoBid = false,
                placedAt = LocalDateTime.now()
            )
        )

        // When
        val request = BuyoutRequest(lotId, bidderId)
        val response = buyoutService.buyout(request)

        // Then
        assertEquals(BidStatus.ACCEPTED, response.status)
        assertEquals(lotId, response.lotId)
        assertEquals(25000.0, response.price)
        assertEquals("Buyout accepted", response.message)
    }

    @Test
    fun `buyout should accept HYBRID lot`() {
        // Given
        val lotId = "lot-123"
        val bidderId = "user-456"
        val lot = org.mockito.kotlin.mock<LotEntity> {
            on { id }.thenReturn(lotId)
            on { status }.thenReturn(LotStatus.ACTIVE)
            on { lotType }.thenReturn(LotType.HYBRID)
            on { buyoutPrice }.thenReturn(25000.0)
        }

        whenever(mongo.findAndModify(
            any<Query>(),
            any<Update>(),
            any<FindAndModifyOptions>(),
            eq(LotEntity::class.java)
        )).thenReturn(lot)

        whenever(bidRepository.save(any<BidEntity>())).thenReturn(
            BidEntity(
                lotId = lotId,
                bidderId = bidderId,
                amount = 25000.0,
                bidType = BidType.INSTANT_BUY,
                status = BidStatus.WON,
                isAutoBid = false,
                placedAt = LocalDateTime.now()
            )
        )

        // When
        val request = BuyoutRequest(lotId, bidderId)
        val response = buyoutService.buyout(request)

        // Then
        assertEquals(BidStatus.ACCEPTED, response.status)
        assertEquals(lotId, response.lotId)
        assertEquals(25000.0, response.price)
    }

    @Test
    fun `buyout should reject AUCTION lot`() {
        // Given
        val lotId = "lot-123"
        val bidderId = "user-456"

        whenever(mongo.findAndModify(
            any<Query>(),
            any<Update>(),
            any<FindAndModifyOptions>(),
            eq(LotEntity::class.java)
        )).thenReturn(null)

        // When
        val request = BuyoutRequest(lotId, bidderId)
        val response = buyoutService.buyout(request)

        // Then
        assertEquals(BidStatus.REJECTED, response.status)
        assertEquals(lotId, response.lotId)
        assertEquals("Lot is not available for buyout", response.message)
        verify(bidRepository, never()).save(any<BidEntity>())
    }
}
