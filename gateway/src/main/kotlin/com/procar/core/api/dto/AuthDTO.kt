package com.procar.core.api.dto

import io.swagger.v3.oas.annotations.media.Schema

data class GatewayUserRegisterRequest(
    @Schema(example = "john_doe") val username: String,
    @Schema(example = "P@ssw0rd!") val password: String
)