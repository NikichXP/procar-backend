package com.procar.auction.repository

import com.procar.auction.document.LotDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface LotRepository : MongoRepository<LotDocument, String> {
    
    @Query("{ 'auction.end_time': { \$lte: ?0, 'status': { \$nin: ['ENDED', 'SOLD', 'UNSOLD', 'CANCELLED'] } }")
    fun findLotsToBeUpdated(endTime: LocalDateTime): List<LotDocument>
    
    @Query("{ 'status': 'ACTIVE', 'auction.end_time': { \$gt: ?0 } }")
    fun findActiveLots(currentTime: LocalDateTime): List<LotDocument>
}
