package com.procar.auction.repository

import com.procar.auction.document.LotEntity
import com.procar.provider.lot.LotStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface LotRepository : MongoRepository<LotEntity, String>, LotRepositoryCustom {

    fun findByStatus(status: LotStatus): List<LotEntity>

}
