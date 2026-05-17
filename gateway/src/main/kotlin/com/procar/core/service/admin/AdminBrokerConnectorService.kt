package com.procar.core.service.admin

import com.procar.user.api.BrokerAdminAPI
import com.procar.user.api.dto.*
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

    suspend fun updateBrokerStatus(id: String, request: UpdateBrokerStatusRequest): BrokerDto =
        brokerController.updateBrokerStatus(id, request)

    suspend fun patchBroker(id: String, request: PatchBrokerRequest): BrokerDto =
        brokerController.patchBroker(id, request)

    suspend fun deleteBroker(id: String) = brokerController.deleteBroker(id)
}
