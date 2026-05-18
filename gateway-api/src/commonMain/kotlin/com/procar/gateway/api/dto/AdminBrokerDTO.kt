package com.procar.gateway.api.dto

import kotlinx.serialization.Serializable

@Serializable
enum class BrokerStatus { ACTIVE, BLOCKED }

@Serializable
data class BrokerDto(
    val id: String,
    val companyName: String,
    val displayName: String,
    val address: String = "",
    val country: String = "",
    val contactEmail: String = "",
    val phones: List<String> = emptyList(),
    val emails: List<String> = emptyList(),
    val status: BrokerStatus = BrokerStatus.ACTIVE,
)

@Serializable
data class CreateBrokerRequest(
    val id: String,
    val companyName: String,
    val displayName: String,
    val address: String = "",
    val country: String = "",
    val contactEmail: String = "",
    val phones: List<String> = emptyList(),
    val emails: List<String> = emptyList(),
)

@Serializable
data class PatchBrokerRequest(
    val companyName: String? = null,
    val displayName: String? = null,
    val address: String? = null,
    val country: String? = null,
    val contactEmail: String? = null,
    val phones: List<String>? = null,
    val emails: List<String>? = null,
    val status: BrokerStatus? = null,
)
