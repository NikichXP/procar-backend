package com.procar.user.api.dto

data class BrokerDto(
    val id: String,
    val companyName: String,
    val displayName: String,
    val address: String,
    val country: String,
    val contactEmail: String,
    val phones: List<String> = emptyList(),
    val emails: List<String> = emptyList(),
    val status: BrokerStatus = BrokerStatus.ACTIVE
)
