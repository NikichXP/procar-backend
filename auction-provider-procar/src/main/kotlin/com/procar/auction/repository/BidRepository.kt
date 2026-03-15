package com.procar.auction.repository

import com.procar.auction.document.BidDocument
import com.procar.provider.bid.BidStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BidRepository : MongoRepository<BidDocument, String> {
    
    fun findByLotId(lotId: String): List<BidDocument>
    
    fun findByLotIdOrderByPlacedAtDesc(lotId: String): List<BidDocument>
    
    fun findByLotIdAndBidderIdAndStatus(lotId: String, bidderId: String, status: BidStatus): List<BidDocument>
    
    @Query("{ 'lot_id': ?0, 'status': 'ACCEPTED' }")
    fun findAcceptedBidsByLotId(lotId: String): List<BidDocument>
    
    fun deleteByLotId(lotId: String)
}
