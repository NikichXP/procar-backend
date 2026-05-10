package com.procar.commons.error

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.HttpStatus

class MongoExceptionLogger(private val mongoTemplate: MongoTemplate) : ExceptionLogger {

    private val log = LoggerFactory.getLogger(MongoExceptionLogger::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun log(status: HttpStatus, ex: Throwable) {
        val stackTrace = ex.stackTrace.take(20).joinToString("\n") { it.toString() }
        val document = ExceptionLogDocument(
            exceptionType = ex::class.java.name,
            reason = ex.message,
            httpStatus = status.value(),
            stackTrace = stackTrace.ifBlank { null },
        )
        scope.launch {
            runCatching { mongoTemplate.save(document) }
                .onFailure { saveError -> log.warn("Failed to persist exception log", saveError) }
        }
    }
}
