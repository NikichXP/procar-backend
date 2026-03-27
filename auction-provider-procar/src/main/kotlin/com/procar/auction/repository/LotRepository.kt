package com.procar.auction.repository

import com.procar.auction.document.LotDocument
import com.procar.provider.lot.LotStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface LotRepository : MongoRepository<LotDocument, String> {
    
    fun findByStatus(status: LotStatus): List<LotDocument>

}
