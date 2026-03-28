package com.procar.provider.admin

import com.procar.provider.common.GeoCoordinates
import java.time.LocalDateTime

data class AdminCreateWarehouseRequest(
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val coordinates: GeoCoordinates? = null,
    val timezone: String,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null
)

data class AdminUpdateWarehouseRequest(
    val name: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val country: String? = null,
    val coordinates: GeoCoordinates? = null,
    val timezone: String? = null,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null
)

data class AdminWarehouseResponse(
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val coordinates: GeoCoordinates?,
    val timezone: String,
    val contactName: String?,
    val contactPhone: String?,
    val contactEmail: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
