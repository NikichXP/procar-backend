package com.procar.user.api.dto

data class BrokerDto(
    val id: String,
    val name: String,
    val address: String,
    val phones: List<String> = emptyList(),
    val emails: List<String> = emptyList()
)
