package com.procar.auction.service

import com.procar.auction.document.BidEntity
import com.procar.auction.document.LotEntity
import com.procar.auction.repository.BidRepository
import com.procar.provider.bid.*
import com.procar.provider.lot.LotStatus
import com.procar.provider.lot.LotType
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BuyoutService(
    private val mongo: MongoTemplate,
    private val bidRepository: BidRepository,
) {
    fun buyout(request: BuyoutRequest): BuyoutResponse {
        val q = Query(
            Criteria.where("_id").`is`(request.lotId)
                .and("status").`is`(LotStatus.ACTIVE)
                .and("lot_type").`in`(LotType.BUYOUT, LotType.HYBRID),
        )
        val updated = mongo.findAndModify(
            q,
            Update().set("status", LotStatus.AWAITING_PAYMENT),
            FindAndModifyOptions.options().returnNew(true),
            LotEntity::class.java,
        ) ?: return BuyoutResponse(
            lotId = request.lotId,
            bidderId = request.bidderId,
            price = 0.0,
            purchasedAt = LocalDateTime.now(),
            status = BidStatus.REJECTED,
            message = "Lot is not available for buyout",
        )
        val price = requireNotNull(updated.buyoutPrice) { "buyoutPrice must be set" }
        val bid = bidRepository.save(
            BidEntity(
                lotId = updated.id,
                bidderId = request.bidderId,
                amount = price,
                bidType = BidType.INSTANT_BUY,
                status = BidStatus.WON,
                isAutoBid = false,
            )
        )
        return BuyoutResponse(
            lotId = updated.id,
            bidderId = request.bidderId,
            price = price,
            purchasedAt = bid.placedAt,
            status = BidStatus.WON,
            message = "Buyout accepted",
        )
    }
}
