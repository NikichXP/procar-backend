package com.procar.provider.common

import java.time.LocalDateTime

data class ApiResponse<T>(
    val data: T,
    val message: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
