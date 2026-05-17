package com.procar.user.entity

import com.procar.user.api.dto.BrokerStatus
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document(collection = "brokers")
data class BrokerEntity(
    @Id
    val id: String,
    @Field("company_name")
    val companyName: String,
    @Field("display_name")
    val displayName: String,
    @Field("address")
    val address: String = "",
    @Field("country")
    val country: String = "",
    @Field("contact_email")
    val contactEmail: String = "",
    @Field("phones")
    val phones: List<String> = emptyList(),
    @Field("emails")
    val emails: List<String> = emptyList(),
    @Field("status")
    val status: BrokerStatus = BrokerStatus.ACTIVE,
    @Field("created_at")
    val createdAt: Instant = Instant.now(),
    @Field("updated_at")
    val updatedAt: Instant = Instant.now()
)
