package com.procar.user.repo

import com.procar.user.entity.BrokerEntity
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.exists
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Repository

@Repository
class BrokerRepository(private val mongoTemplate: MongoTemplate) {

    fun save(broker: BrokerEntity): BrokerEntity = mongoTemplate.save(broker)

    fun findById(id: String): BrokerEntity? = mongoTemplate.findById<BrokerEntity>(id)

    fun findAll(): List<BrokerEntity> = mongoTemplate.findAll<BrokerEntity>()

    fun existsById(id: String): Boolean =
        mongoTemplate.exists<BrokerEntity>(Query(Criteria.where("_id").`is`(id)))

    fun deleteById(id: String) {
        mongoTemplate.remove<BrokerEntity>(Query(Criteria.where("_id").`is`(id)))
    }
}
