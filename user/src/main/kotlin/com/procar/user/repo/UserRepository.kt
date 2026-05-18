package com.procar.user.repo

import com.procar.user.entity.UserEntity
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Repository

@Repository
class UserRepository(private val mongoTemplate: MongoTemplate) {

    suspend fun save(userEntity: UserEntity): UserEntity {
        // TODO: Use withContext(Dispatchers.IO) here since MongoTemplate is blocking
        // For now, this will work but could block the thread
        return mongoTemplate.save(userEntity)
    }

    suspend fun findById(id: String): UserEntity? {
        val query = Query(Criteria.where("id").`is`(id))
        return mongoTemplate.findOne<UserEntity>(query)
    }

    suspend fun findByUsername(username: String): UserEntity? {
        val query = Query(Criteria.where("username").`is`(username))
        return mongoTemplate.findOne<UserEntity>(query)
    }

    suspend fun findAll(): List<UserEntity> {
        return mongoTemplate.findAll(UserEntity::class.java)
    }

    suspend fun deleteById(id: String) {
        val query = Query(Criteria.where("id").`is`(id))
        mongoTemplate.remove<UserEntity>(query)
    }
}
