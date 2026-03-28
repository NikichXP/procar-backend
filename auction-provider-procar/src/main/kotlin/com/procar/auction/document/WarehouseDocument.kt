package com.procar.auction.document

import com.procar.provider.common.GeoCoordinates
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Document(collection = "warehouses")
data class WarehouseDocument(
    @Id
    @OptIn(ExperimentalUuidApi::class)
    val id: String = Uuid.generateV7().toString(),

    @Field("name")
    val name: String,

    @Field("address")
    val address: String,

    @Field("city")
    val city: String,

    @Field("state")
    val state: String,

    @Field("zip_code")
    val zipCode: String,

    @Field("country")
    val country: String,

    @Field("coordinates")
    val coordinates: GeoCoordinates?,

    @Field("timezone")
    val timezone: String,

    @Field("contact_name")
    val contactName: String?,

    @Field("contact_phone")
    val contactPhone: String?,

    @Field("contact_email")
    val contactEmail: String?,

    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Field("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
