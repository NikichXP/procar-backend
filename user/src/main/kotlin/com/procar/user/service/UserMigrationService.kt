package com.procar.user.service

import com.procar.user.api.dto.UserRole
import com.procar.user.entity.UserEntity
import com.procar.user.repo.UserRepository
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service

// TODO после релиза рефакторинга можно удалить нахер
@Service
class UserMigrationService(
    private val mongoTemplate: MongoTemplate,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(UserMigrationService::class.java)
    private val scope = CoroutineScope(Dispatchers.IO)

    @PostConstruct
    fun migrate() {
        scope.launch {
            try {
                migrateUsers()
            } catch (e: Exception) {
                logger.error("Failed to migrate users", e)
            }
        }
    }

    private suspend fun migrateUsers() {
        logger.info("Starting user migration to versioned entities...")
        
        // 1. Fix documents missing the 'role' field (old schema)
        val queryMissingRole = Query(Criteria.where("role").exists(false))
        val updateToCustomer = Update().set("role", UserRole.CUSTOMER.name)
        val resultRole = mongoTemplate.updateMulti(queryMissingRole, updateToCustomer, "users")
        if (resultRole.modifiedCount > 0) {
            logger.info("Migrated ${resultRole.modifiedCount} users missing 'role' field to CUSTOMER")
        }

        // 2. Fix documents missing the 'version' field (new VersionedEntity requirement)
        val queryMissingVersion = Query(Criteria.where("version").exists(false))
        val updateVersion = Update().set("version", 1)
        val resultVersion = mongoTemplate.updateMulti(queryMissingVersion, updateVersion, "users")
        if (resultVersion.modifiedCount > 0) {
            logger.info("Migrated ${resultVersion.modifiedCount} users to version 1")
        }

        logger.info("User migration completed.")
    }
}
