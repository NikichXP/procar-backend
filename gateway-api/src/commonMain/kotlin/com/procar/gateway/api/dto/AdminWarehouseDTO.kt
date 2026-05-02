package com.procar.gateway.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class AdminWarehouseResponse(
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val timezone: String,
    val createdAt: String,
    val updatedAt: String,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
)

@Serializable
data class AdminCreateWarehouseRequest(
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val timezone: String,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
)
