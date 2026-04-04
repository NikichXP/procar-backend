package com.procar.admin.service

import com.procar.provider.admin.AdminCreateLotRequest
import com.procar.provider.admin.AdminCreateWarehouseRequest
import com.procar.provider.admin.AdminLotResponse
import com.procar.provider.admin.AdminPaginatedLotsResponse
import com.procar.provider.admin.AdminWarehouseResponse
import com.procar.provider.bid.BidHistoryResponse
import com.procar.provider.bid.ProviderBid
import com.procar.provider.common.ApiResponse
import com.procar.provider.common.PaginationResponse
import com.procar.auth.api.dto.AuthResult
import com.procar.auth.api.dto.LoginRequest
import com.procar.auth.api.dto.RegisterRequest
import com.procar.auth.api.dto.AccessToken
import com.procar.auth.api.dto.RefreshRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class GatewayClientService(
    @Value($$"${gateway.base-url:http://localhost:8080}") private val gatewayBaseUrl: String
) {

    private val logger = LoggerFactory.getLogger(GatewayClientService::class.java)

    private val webClient = WebClient.builder()
        .baseUrl(gatewayBaseUrl)
        .build()

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
                .awaitBody<ApiResponse<AdminPaginatedLotsResponse>>()
                .data
        } catch (e: Exception) {
            logger.error("Failed to fetch lots: ${e.message}")
            AdminPaginatedLotsResponse(listOf(), PaginationResponse(false))
        }

        logger.info("Received ${response.lots.size} lots")

        // TODO WHAT THE HELL IS THIS, it is emitting, why can't I see that in UI?
        response.lots.forEach { emit(it) }
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

    suspend fun getWarehouses(): List<AdminWarehouseResponse> {
        return try {
            webClient.get()
                .uri("/api/admin/warehouses")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBody<ApiResponse<List<AdminWarehouseResponse>>>()
                .data
        } catch (e: Exception) {
            logger.error("Failed to fetch warehouses: ${e.message}")
            emptyList()
        }
    }

    suspend fun createWarehouse(request: AdminCreateWarehouseRequest): AdminWarehouseResponse {
        return webClient.post()
            .uri("/api/admin/warehouses")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBody<ApiResponse<AdminWarehouseResponse>>()
            .data
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

    // Auth management methods
    suspend fun login(loginRequest: LoginRequest): AuthResult {
        return try {
            webClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBody<AuthResult>()
        } catch (e: Exception) {
            logger.error("Failed to login: ${e.message}")
            AuthResult(success = false, message = "Login failed: ${e.message}")
        }
    }

    suspend fun register(registerRequest: RegisterRequest): AuthResult {
        return try {
            webClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerRequest)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBody<AuthResult>()
        } catch (e: Exception) {
            logger.error("Failed to register: ${e.message}")
            AuthResult(success = false, message = "Registration failed: ${e.message}")
        }
    }

    suspend fun refreshAccess(refreshRequest: RefreshRequest): AccessToken {
        return try {
            webClient.post()
                .uri("/auth/access")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(refreshRequest)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBody<AccessToken>()
        } catch (e: Exception) {
            logger.error("Failed to refresh access token: ${e.message}")
            throw e
        }
    }

    suspend fun logout(authorization: String) {
        try {
            webClient.post()
                .uri("/auth/logout")
                .header("Authorization", authorization)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBody<Void>()
        } catch (e: Exception) {
            logger.error("Failed to logout: ${e.message}")
            throw e
        }
    }

    suspend fun logoutAllDevices(authorization: String): Map<String, Any> {
        return try {
            webClient.post()
                .uri("/auth/logout-all")
                .header("Authorization", authorization)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBody<Map<String, Any>>()
        } catch (e: Exception) {
            logger.error("Failed to logout all devices: ${e.message}")
            throw e
        }
    }
}
