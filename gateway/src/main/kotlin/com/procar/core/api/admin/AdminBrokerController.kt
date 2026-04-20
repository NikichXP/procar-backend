package com.procar.core.api.admin

import com.procar.core.service.admin.AdminBrokerConnectorService
import com.procar.user.api.dto.BrokerDto
import com.procar.user.api.dto.CreateBrokerRequest
import com.procar.user.api.dto.UpdateBrokerRequest
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

    @PutMapping("/{id}")
    suspend fun updateBroker(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateBrokerRequest
    ): BrokerDto = adminBrokerConnectorService.updateBroker(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteBroker(@PathVariable id: String) {
        adminBrokerConnectorService.deleteBroker(id)
    }
}
