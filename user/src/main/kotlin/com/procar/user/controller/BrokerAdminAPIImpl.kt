package com.procar.user.controller

import com.procar.user.api.BrokerAdminAPI
import com.procar.user.api.dto.BrokerDto
import com.procar.user.api.dto.CreateBrokerRequest
import com.procar.user.api.dto.UpdateBrokerRequest
import com.procar.user.service.BrokerService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class BrokerAdminAPIImpl(private val brokerService: BrokerService) : BrokerAdminAPI {

    override suspend fun getBrokers(): List<BrokerDto> = brokerService.getBrokers()

    override suspend fun getBroker(@PathVariable id: String): BrokerDto =
        brokerService.getBroker(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Broker not found")

    override suspend fun createBroker(@Valid @RequestBody request: CreateBrokerRequest): BrokerDto =
        brokerService.createBroker(request)

    override suspend fun updateBroker(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateBrokerRequest
    ): BrokerDto = brokerService.updateBroker(id, request)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Broker not found")

    @ResponseStatus(HttpStatus.NO_CONTENT)
    override suspend fun deleteBroker(@PathVariable id: String) {
        if (!brokerService.deleteBroker(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Broker not found")
        }
    }
}
