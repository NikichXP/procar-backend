package com.procar.user.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document(collection = "brokers")
data class BrokerEntity(
    @Id
    val id: String,
    @Field("name")
    val name: String,
    @Field("address")
    val address: String = "",
    @Field("phones")
    val phones: List<String> = emptyList(),
    @Field("emails")
    val emails: List<String> = emptyList(),
    @Field("created_at")
    val createdAt: Instant = Instant.now(),
    @Field("updated_at")
    val updatedAt: Instant = Instant.now()
)
