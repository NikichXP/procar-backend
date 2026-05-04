package com.procar.auction.repository

import com.procar.auction.document.LotEntity
import com.procar.provider.lot.LotStatus
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAndModify
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class LotRepositoryCustomImpl(private val mongoTemplate: MongoTemplate) : LotRepositoryCustom {

    override fun lockForPayment(lotId: String): Boolean {
        return mongoTemplate.findAndModify<LotEntity>(
            Query(Criteria
                .where("_id").`is`(lotId)
                .and(LotEntity::status.name).`is`(LotStatus.ACTIVE)),
            Update()
                .set(LotEntity::status.name, LotStatus.AWAITING_PAYMENT)
                .set(LotEntity::updatedAt.name, LocalDateTime.now()),
            FindAndModifyOptions.options().returnNew(false)
        ) != null
    }
}
