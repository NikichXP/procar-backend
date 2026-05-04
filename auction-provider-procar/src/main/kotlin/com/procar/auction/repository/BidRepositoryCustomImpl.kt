package com.procar.auction.repository

import com.procar.auction.document.AuctionInfoDocument
import com.procar.auction.document.LotEntity
import com.procar.provider.lot.LotStatus
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAndModify
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import java.time.LocalDateTime

class BidRepositoryCustomImpl(private val mongoTemplate: MongoTemplate) : BidRepositoryCustom {

    override fun advanceBid(lotId: String, amount: Double, closeOnWin: Boolean): Boolean {
        val auctionPrefix = LotEntity::auction.name
        val criteria = Criteria
            .where("_id").`is`(lotId)
            .and(LotEntity::status.name).`is`(LotStatus.ACTIVE)
            .and("$auctionPrefix.${AuctionInfoDocument::endTime.name}").gt(LocalDateTime.now())
            .and("$auctionPrefix.${AuctionInfoDocument::currentBid.name}").lt(amount)

        val update = Update()
            .set("$auctionPrefix.${AuctionInfoDocument::currentBid.name}", amount)
            .inc("$auctionPrefix.${AuctionInfoDocument::totalBids.name}", 1)
            .set(LotEntity::updatedAt.name, LocalDateTime.now())

        if (closeOnWin) {
            update.set(LotEntity::status.name, LotStatus.AWAITING_PAYMENT)
        }

        return mongoTemplate.findAndModify<LotEntity>(
            Query(criteria),
            update,
            FindAndModifyOptions.options().returnNew(false)
        ) != null
    }
}
