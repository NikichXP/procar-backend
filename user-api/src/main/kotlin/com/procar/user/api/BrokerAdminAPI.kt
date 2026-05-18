package com.procar.user.api

import com.procar.user.api.dto.*
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.*

@HttpExchange("/api/admin/brokers")
interface BrokerAdminAPI {

    @GetExchange
    suspend fun getBrokers(): List<BrokerDto>

    @GetExchange("/{id}")
    suspend fun getBroker(@PathVariable id: String): BrokerDto

    @PostExchange
    suspend fun createBroker(@Valid @RequestBody request: CreateBrokerRequest): BrokerDto

    @PostExchange("/{id}/status")
    suspend fun updateBrokerStatus(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateBrokerStatusRequest
    ): BrokerDto

    @PatchExchange("/{id}")
    suspend fun patchBroker(
        @PathVariable id: String,
        @Valid @RequestBody request: PatchBrokerRequest
    ): BrokerDto

    @DeleteExchange("/{id}")
    suspend fun deleteBroker(@PathVariable id: String)
}
