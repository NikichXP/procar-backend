package com.procar.auction.repository

import com.procar.auction.document.BidEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BidRepository : MongoRepository<BidEntity, String>, BidRepositoryCustom {

    fun findByLotId(lotId: String): List<BidEntity>

    fun findByLotIdOrderByPlacedAtDesc(lotId: String): List<BidEntity>

    fun deleteByLotId(lotId: String)
}
