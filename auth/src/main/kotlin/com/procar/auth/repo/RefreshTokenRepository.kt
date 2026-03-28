package com.procar.auth.repo

import com.procar.auth.entity.RefreshToken
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class RefreshTokenRepository(private val mongoTemplate: MongoTemplate) {

    fun save(refreshToken: RefreshToken): RefreshToken {
        mongoTemplate.save(refreshToken)
        return refreshToken
    }

    fun findByToken(token: String): RefreshToken? {
        val query = Query(Criteria.where("token").`is`(token))
        return mongoTemplate.findOne<RefreshToken>(query)
    }

    fun findByUserId(userId: String): List<RefreshToken> {
        val query = Query(Criteria.where("userId").`is`(userId))
        return mongoTemplate.find<RefreshToken>(query)
    }

    fun deleteByToken(token: String): Boolean {
        val query = Query(Criteria.where("token").`is`(token))
        val result = mongoTemplate.remove<RefreshToken>(query)
        return result.deletedCount > 0
    }

    fun deleteByUserId(userId: String): Boolean {
        val query = Query(Criteria.where("userId").`is`(userId))
        val result = mongoTemplate.remove<RefreshToken>(query)
        return result.deletedCount > 0
    }

    fun deleteExpiredTokens(): Long {
        val query = Query(Criteria.where("expiresAt").lt(Instant.now()))
        val result = mongoTemplate.remove<RefreshToken>(query)
        return result.deletedCount
    }
}
