package com.procar.auth.repo

import com.procar.auth.entity.PasswordAuthReason
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class PasswordAuthRepository(private val mongoTemplate: MongoTemplate) {

    fun findByUserId(userId: String): PasswordAuthReason? {
        val query = Query(Criteria.where(PasswordAuthReason::userId.name).`is`(userId))
        return mongoTemplate.findOne(query, PasswordAuthReason::class.java)
    }

    fun findByUsername(username: String): PasswordAuthReason? {
        val query = Query(Criteria.where(PasswordAuthReason::username.name).`is`(username))
        return mongoTemplate.findOne(query, PasswordAuthReason::class.java)
    }

    fun save(authReason: PasswordAuthReason): PasswordAuthReason {
        mongoTemplate.save(authReason)
        return authReason
    }

    fun deleteByUserId(userId: String): Boolean {
        val query = Query(Criteria.where(PasswordAuthReason::userId.name).`is`(userId))
        val result = mongoTemplate.remove(query, PasswordAuthReason::class.java)
        return result.deletedCount > 0
    }

    fun updateLastLogin(userId: String): Boolean {
        val query = Query(Criteria.where(PasswordAuthReason::userId.name).`is`(userId))
        val update = Update.update(PasswordAuthReason::lastLogin.name, LocalDateTime.now())
        val result = mongoTemplate.updateFirst(query, update, PasswordAuthReason::class.java)
        return result.modifiedCount > 0
    }
}