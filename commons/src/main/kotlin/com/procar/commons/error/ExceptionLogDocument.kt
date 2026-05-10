package com.procar.commons.error

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "exception_logs")
data class ExceptionLogDocument(
    @Id val id: String? = null,
    val timestamp: Instant = Instant.now(),
    val exceptionType: String,
    val reason: String?,
    val httpStatus: Int,
    val stackTrace: String?,
)
