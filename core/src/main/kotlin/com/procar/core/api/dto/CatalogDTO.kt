package com.procar.core.api.dto

data class Brand(
    val id: String,
    val name: String,
    val logoUrl: String? = null,
    val lotsCount: Int
)

data class Model(
    val id: String,
    val name: String,
    val brandId: String,
    val lotsCount: Int
)
