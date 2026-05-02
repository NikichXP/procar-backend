package com.procar.gateway.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class BrokerDto(
    val id: String,
    val name: String,
    val address: String = "",
    val phones: List<String> = emptyList(),
    val emails: List<String> = emptyList(),
)

@Serializable
data class CreateBrokerRequest(
    val id: String,
    val name: String,
    val address: String = "",
    val phones: List<String> = emptyList(),
    val emails: List<String> = emptyList(),
)

@Serializable
data class UpdateBrokerRequest(
    val name: String? = null,
    val address: String? = null,
    val phones: List<String>? = null,
    val emails: List<String>? = null,
)
