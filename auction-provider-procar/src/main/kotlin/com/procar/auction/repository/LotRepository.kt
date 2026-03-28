package com.procar.auction.repository

import com.procar.auction.document.LotDocument
import com.procar.provider.lot.LotStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface LotRepository : MongoRepository<LotDocument, String> {
    
    fun findByStatus(status: LotStatus): List<LotDocument>

}
