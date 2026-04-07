package com.procar.api

import com.procar.model.AdminCreateLotRequest
import com.procar.model.AdminCreateWarehouseRequest
import com.procar.model.AdminLotResponse
import com.procar.model.AdminPaginatedLotsResponse
import com.procar.model.AdminWarehouseResponse
import com.procar.model.ApiResponse
import com.procar.model.AuthResult
import com.procar.model.LoginRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val GATEWAY_BASE_URL: String = js("(typeof GATEWAY_BASE_URL !== 'undefined') ? GATEWAY_BASE_URL : 'http://localhost:8080'")

object AuthState {
    var accessToken: String = ""
    var refreshToken: String = ""
}

val httpClient = HttpClient {
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

suspend fun login(username: String, password: String): AuthResult {
    return httpClient.post("$GATEWAY_BASE_URL/auth/login") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(LoginRequest(username, password))
    }.body()
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
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun createWarehouse(request: AdminCreateWarehouseRequest): AdminWarehouseResponse {
    return httpClient.post("$GATEWAY_BASE_URL/api/admin/warehouses") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(request)
    }.body()
}
