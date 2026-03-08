package com.procar.core.api.dto

data class Pagination(
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int
)

data class Error(
    val code: String,
    val message: String,
    val timestamp: String
)