package com.procar.auction.migration

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component

@Component
class LotTypeMigration(private val mongo: MongoTemplate) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(javaClass)
    override fun run(vararg args: String) {
        val coll = "lots"
        val pending = Query(Criteria.where("lot_type").exists(false))
        val docs = mongo.find(pending, org.bson.Document::class.java, coll)
        if (docs.isEmpty()) return
        log.info("LotTypeMigration: migrating {} lot(s)", docs.size)
        docs.forEach { doc ->
            val auction = doc.get("auction", org.bson.Document::class.java)
            val oldType = auction?.getString("auction_type")
            val bin = auction?.get("buy_it_now_price") as? Number
            val lotType = when {
                oldType == "BUY_IT_NOW" -> "BUYOUT"
                bin != null             -> "HYBRID"
                else                    -> "AUCTION"
            }
            val update = Update().set("lot_type", lotType)
            if (bin != null) update.set("buyout_price", bin.toDouble())
            if (lotType == "BUYOUT") update.set("auction", null)
            mongo.updateFirst(
                Query(Criteria.where("_id").`is`(doc.getString("_id"))),
                update, coll,
            )
        }
        log.info("LotTypeMigration: done")
        
        // Second pass: unset legacy Mongo fields on already-migrated documents
        val legacyCleanup = mongo.updateMulti(
            Query(Criteria.where("auction.auction_type").exists(true)),
            Update().unset("auction.auction_type").unset("auction.buy_it_now_price"),
            coll,
        )
        if (legacyCleanup.modifiedCount > 0) {
            log.info("LotTypeMigration: cleaned up legacy fields from {} lot(s)", legacyCleanup.modifiedCount)
        }
    }
}
