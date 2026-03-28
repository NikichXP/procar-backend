package com.procar.auction.repository

import com.procar.auction.document.WarehouseDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface WarehouseRepository : MongoRepository<WarehouseDocument, String>
