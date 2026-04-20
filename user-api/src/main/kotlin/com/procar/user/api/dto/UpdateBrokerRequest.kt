package com.procar.user.api.dto

data class UpdateBrokerRequest(
    val name: String? = null,
    val address: String? = null,
    val phones: List<String>? = null,
    val emails: List<String>? = null
)
