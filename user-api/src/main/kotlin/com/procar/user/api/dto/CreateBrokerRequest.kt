package com.procar.user.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class CreateBrokerRequest(
    @field:NotBlank
    @field:Pattern(regexp = "[a-z0-9\\-]+", message = "id must match [a-z0-9\\-]+")
    val id: String,
    @field:NotBlank
    val companyName: String,
    @field:NotBlank
    val displayName: String,
    val address: String = "",
    val country: String = "",
    @field:NotBlank
    val contactEmail: String,
    val phones: List<String> = emptyList(),
    val emails: List<String> = emptyList()
)
