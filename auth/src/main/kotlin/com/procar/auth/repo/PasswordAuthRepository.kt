package com.procar.auth.repo

import com.procar.auth.entity.PasswordAuthReason
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class PasswordAuthRepository(private val mongoTemplate: MongoTemplate) {

    fun findByUserId(userId: String): PasswordAuthReason? {
        val query = Query(Criteria.where("id").`is`(userId)) // Use username as ID
        return mongoTemplate.findOne(query, PasswordAuthReason::class.java)
    }

    fun save(authReason: PasswordAuthReason): PasswordAuthReason {
        mongoTemplate.save(authReason)
        return authReason
    }

    fun deleteByUserId(userId: String): Boolean {
        val query = Query(Criteria.where("userId").`is`(userId))
        val result = mongoTemplate.remove(query, PasswordAuthReason::class.java)
        return result.deletedCount > 0
    }
}