package com.procar.auth.repo

import com.procar.auth.entity.RefreshToken
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class RefreshTokenRepository(private val mongoTemplate: MongoTemplate) {

    fun save(refreshToken: RefreshToken): RefreshToken {
        mongoTemplate.save(refreshToken)
        return refreshToken
    }

    fun findByToken(token: String): RefreshToken? {
        val query = Query(Criteria.where("token").`is`(token))
        return mongoTemplate.findOne(query, RefreshToken::class.java)
    }

    fun findByUserId(userId: String): List<RefreshToken> {
        val query = Query(Criteria.where("userId").`is`(userId))
        return mongoTemplate.find(query, RefreshToken::class.java)
    }

    fun deleteByToken(token: String): Boolean {
        val query = Query(Criteria.where("token").`is`(token))
        val result = mongoTemplate.remove(query, RefreshToken::class.java)
        return result.deletedCount > 0
    }

    fun deleteByUserId(userId: String): Boolean {
        val query = Query(Criteria.where("userId").`is`(userId))
        val result = mongoTemplate.remove(query, RefreshToken::class.java)
        return result.deletedCount > 0
    }
}
