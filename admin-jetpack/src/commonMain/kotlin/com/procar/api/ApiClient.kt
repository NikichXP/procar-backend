package com.procar.api

import com.procar.TokenStorage
import com.procar.createHttpClient
import com.procar.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object GatewayConfig {
    var baseUrl: String = "http://localhost:8080"
}

val GATEWAY_BASE_URL: String
    get() = GatewayConfig.baseUrl

object AuthState {
    var accessToken: String = ""

    var refreshToken: String
        get() = TokenStorage.getItem("refreshToken") ?: ""
        set(value) {
            if (value.isEmpty()) {
                TokenStorage.removeItem("refreshToken")
            } else {
                TokenStorage.setItem("refreshToken", value)
            }
        }
}

val httpClient: HttpClient by lazy {
    createHttpClient().config {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        defaultRequest {
            if (AuthState.accessToken.isNotEmpty()) {
                header(HttpHeaders.Authorization, "Bearer ${AuthState.accessToken}")
            }
        }
    }
}

suspend fun login(username: String, password: String): AuthResult {
    val response = httpClient.post("$GATEWAY_BASE_URL/auth/login") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(LoginRequest(username, password))
    }

    if (response.status == HttpStatusCode.Unauthorized) {
        throw Exception("Invalid username or password")
    }

    return response.body()
}

suspend fun getAccessToken(refreshToken: String): AccessToken {
    val response = httpClient.post("$GATEWAY_BASE_URL/auth/access") {
        parameter("refreshToken", refreshToken)
    }

    if (response.status != HttpStatusCode.OK) {
        throw Exception("Failed to refresh access token")
    }

    return response.body()
}

suspend fun fetchLots(): List<AdminLotResponse> {
    return try {
        val response: ApiResponse<AdminPaginatedLotsResponse> =
            httpClient.get("$GATEWAY_BASE_URL/api/admin/lots?limit=100").body()
        response.data.lots
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun createLot(request: AdminCreateLotRequest): AdminLotResponse {
    return httpClient.post("$GATEWAY_BASE_URL/api/admin/lots") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(request)
    }.body()
}

suspend fun fetchWarehouses(): List<AdminWarehouseResponse> {
    return try {
        val response: ApiResponse<List<AdminWarehouseResponse>> =
            httpClient.get("$GATEWAY_BASE_URL/api/admin/warehouses").body()
        response.data
    } catch (_: Exception) {
        emptyList()
    }
}

suspend fun createWarehouse(request: AdminCreateWarehouseRequest): AdminWarehouseResponse {
    return httpClient.post("$GATEWAY_BASE_URL/api/admin/warehouses") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(request)
    }.body()
}
