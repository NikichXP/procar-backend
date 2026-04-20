package com.procar.core.service.admin

import com.procar.user.api.BrokerAdminAPI
import com.procar.user.api.dto.BrokerDto
import com.procar.user.api.dto.CreateBrokerRequest
import com.procar.user.api.dto.UpdateBrokerRequest
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class AdminBrokerConnectorService(
    @Qualifier("brokerHttpClient") private val brokerController: BrokerAdminAPI
) {

    suspend fun getBrokers(): List<BrokerDto> = brokerController.getBrokers()

    suspend fun getBroker(id: String): BrokerDto = brokerController.getBroker(id)

    suspend fun createBroker(request: CreateBrokerRequest): BrokerDto =
        brokerController.createBroker(request)

    suspend fun updateBroker(id: String, request: UpdateBrokerRequest): BrokerDto =
        brokerController.updateBroker(id, request)

    suspend fun deleteBroker(id: String) = brokerController.deleteBroker(id)
}
