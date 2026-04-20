package com.procar.user.api

import com.procar.user.api.dto.BrokerDto
import com.procar.user.api.dto.CreateBrokerRequest
import com.procar.user.api.dto.UpdateBrokerRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.DeleteExchange
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

@HttpExchange("/api/admin/brokers")
interface BrokerAdminAPI {

    @GetExchange
    suspend fun getBrokers(): List<BrokerDto>

    @GetExchange("/{id}")
    suspend fun getBroker(@PathVariable id: String): BrokerDto

    @PostExchange
    suspend fun createBroker(@Valid @RequestBody request: CreateBrokerRequest): BrokerDto

    @PutExchange("/{id}")
    suspend fun updateBroker(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateBrokerRequest
    ): BrokerDto

    @DeleteExchange("/{id}")
    suspend fun deleteBroker(@PathVariable id: String)
}
