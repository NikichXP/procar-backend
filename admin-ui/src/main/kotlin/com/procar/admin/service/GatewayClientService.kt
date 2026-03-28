package com.procar.admin.service

import com.procar.provider.admin.AdminCreateLotRequest
import com.procar.provider.admin.AdminLotResponse
import com.procar.provider.admin.AdminPaginatedLotsResponse
import com.procar.provider.bid.BidHistoryResponse
import com.procar.provider.bid.ProviderBid
import com.procar.provider.common.ApiResponse
import com.procar.provider.common.PaginationResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class GatewayClientService(
    @Value("\${gateway.base-url:http://localhost:8080}") private val gatewayBaseUrl: String
) {

    private val logger = LoggerFactory.getLogger(GatewayClientService::class.java)

    private val webClient = WebClient.builder()
        .baseUrl(gatewayBaseUrl)
        .build()

    suspend fun getUsers(): Flow<String> {
        // TODO: Implement when admin user API is available
        return flowOf()
    }

    suspend fun getLots(status: String? = null): Flow<AdminLotResponse> = flow {
        logger.info("Fetching lots with status: $status")
        val response = try {
            webClient.get()
                .uri { uriBuilder ->
                    val builder = uriBuilder.path("/api/admin/lots")
                        .queryParam("limit", 100)
                    status?.let { builder.queryParam("status", it) }
                    builder.build()
                }
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono<ApiResponse<AdminPaginatedLotsResponse>>()
                .awaitFirst()
                ?.data
        } catch (e: Exception) {
            logger.error("Failed to fetch lots: ${e.message}")
            AdminPaginatedLotsResponse(listOf(), PaginationResponse(false))
        }

        logger.info("Received ${response?.lots?.size ?: -1} lots")

        // TODO WHAT THE HELL IS THIS, it is emitting, why can't I see that in UI?
        response?.lots?.forEach { emit(it) }
    }

    suspend fun createLot(request: AdminCreateLotRequest): AdminLotResponse {
        return webClient.post()
            .uri("/api/admin/lots")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono<AdminLotResponse>()
            .awaitFirst()
    }

    suspend fun getBidsForLot(lotId: String): Flow<ProviderBid> = flow {
        logger.info("Fetching bids for lot: $lotId")
        val response = webClient.get()
            .uri("/api/admin/bids/lot/{lotId}", lotId)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono<BidHistoryResponse>()
            .awaitFirst()

        logger.info("Received ${response.bids.size} bids for lot $lotId")
        response.bids.forEach { emit(it) }
    }
}
