package com.procar.api

import com.procar.model.AdminCreateLotRequest
import com.procar.model.AdminCreateWarehouseRequest
import com.procar.model.AdminLotResponse
import com.procar.model.AdminPaginatedLotsResponse
import com.procar.model.AdminWarehouseResponse
import com.procar.model.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

const val GATEWAY_BASE_URL = "http://localhost:8080"

val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
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
