package com.procar.gateway.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class Pagination(
    @ApiDoc(example = "0") val page: Int,
    @ApiDoc(example = "20") val size: Int,
    @ApiDoc(example = "150") val totalElements: Int,
    @ApiDoc(example = "8") val totalPages: Int,
)

@Serializable
data class Error(
    val code: String,
    val message: String,
    val timestamp: String,
)

@Serializable
data class ApiResponse<T>(val data: T)

@Serializable
data class PaginationResponse(
    val hasNext: Boolean,
    val nextCursor: String? = null,
)
