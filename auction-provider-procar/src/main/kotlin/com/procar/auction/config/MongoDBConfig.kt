package com.procar.auction.config

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoRepositories("com.procar.auction.repository")
class MongoDBConfig : AbstractMongoClientConfiguration() {

    @Value("\${spring.data.mongodb.uri:mongodb://localhost:27017/auction_procar}")
    private lateinit var mongoUri: String

    @Value("\${spring.data.mongodb.database:auction_procar}")
    private lateinit var databaseName: String

    override fun mongoClient(): MongoClient {
        return MongoClients.create(mongoUri)
    }

    override fun getDatabaseName(): String {
        return databaseName
    }

    @Bean
    fun mongoTemplate(): MongoTemplate {
        return MongoTemplate(mongoClient(), databaseName)
    }
}
