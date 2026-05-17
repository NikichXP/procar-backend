package com.procar.user.api.dto

data class PatchBrokerRequest(
    val companyName: String? = null,
    val displayName: String? = null,
    val address: String? = null,
    val country: String? = null,
    val contactEmail: String? = null,
    val phones: List<String>? = null,
    val emails: List<String>? = null,
    val status: BrokerStatus? = null
)
