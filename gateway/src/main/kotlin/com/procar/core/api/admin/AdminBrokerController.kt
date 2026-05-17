package com.procar.core.api.admin

import com.procar.core.service.admin.AdminBrokerConnectorService
import com.procar.user.api.dto.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/brokers")
class AdminBrokerController(
    private val adminBrokerConnectorService: AdminBrokerConnectorService
) {

    @GetMapping
    suspend fun getBrokers(): List<BrokerDto> = adminBrokerConnectorService.getBrokers()

    @GetMapping("/{id}")
    suspend fun getBroker(@PathVariable id: String): BrokerDto =
        adminBrokerConnectorService.getBroker(id)

    @PostMapping
    suspend fun createBroker(@Valid @RequestBody request: CreateBrokerRequest): BrokerDto =
        adminBrokerConnectorService.createBroker(request)

    @PostMapping("/{id}/status")
    suspend fun updateBrokerStatus(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateBrokerStatusRequest
    ): BrokerDto = adminBrokerConnectorService.updateBrokerStatus(id, request)

    @PatchMapping("/{id}")
    suspend fun patchBroker(
        @PathVariable id: String,
        @Valid @RequestBody request: PatchBrokerRequest
    ): BrokerDto = adminBrokerConnectorService.patchBroker(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteBroker(@PathVariable id: String) {
        adminBrokerConnectorService.deleteBroker(id)
    }
}
