package com.procar.customer.api

import coil3.ImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.procar.customer.createHttpClient
import com.procar.customer.getPlatformContext
import com.procar.gateway.api.dto.AuthResult
import com.procar.gateway.api.dto.CarCondition
import com.procar.gateway.api.dto.GatewayUserRegisterRequest
import com.procar.gateway.api.dto.LotDetail
import com.procar.gateway.api.dto.LotPage
import com.procar.gateway.api.dto.LotSummary
import com.procar.gateway.api.dto.LotStatus
import com.procar.gateway.api.dto.LoginRequest
import com.procar.gateway.api.dto.UserBidPage
import com.procar.gateway.api.dto.UserInfoDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object GatewayConfig {
    var baseUrl: String = "http://localhost:8080"
}

object AuthToken {
    var accessToken: String? = null
}

val httpClient: HttpClient by lazy {
    createHttpClient().config {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }
}

val imageLoader: ImageLoader by lazy {
    ImageLoader.Builder(getPlatformContext())
        .components { add(KtorNetworkFetcherFactory(httpClient)) }
        .build()
}

fun absoluteImageUrl(url: String): String =
    if (url.startsWith("http://") || url.startsWith("https://")) url
    else "${GatewayConfig.baseUrl}$url"

const val DEFAULT_PAGE_SIZE: Int = 20

suspend fun fetchLots(
    page: Int = 0,
    size: Int = DEFAULT_PAGE_SIZE,
    status: LotStatus? = null,
    condition: CarCondition? = null,
    brandId: String? = null,
    modelId: String? = null,
    yearFrom: Int? = null,
    yearTo: Int? = null,
    priceFrom: Double? = null,
    priceTo: Double? = null,
    sort: String = "endTime,asc",
): LotPage = httpClient.get("${GatewayConfig.baseUrl}/api/lots") {
    parameter("page", page)
    parameter("size", size)
    parameter("sort", sort)
    if (status != null) parameter("status", status.name)
    if (condition != null) parameter("condition", condition.name)
    if (brandId != null) parameter("brandId", brandId)
    if (modelId != null) parameter("modelId", modelId)
    if (yearFrom != null) parameter("yearFrom", yearFrom)
    if (yearTo != null) parameter("yearTo", yearTo)
    if (priceFrom != null) parameter("priceFrom", priceFrom)
    if (priceTo != null) parameter("priceTo", priceTo)
}.body()

suspend fun fetchLotDetail(lotId: String): LotDetail =
    httpClient.get("${GatewayConfig.baseUrl}/api/lots/$lotId").body()

suspend fun login(request: LoginRequest): AuthResult =
    httpClient.post("${GatewayConfig.baseUrl}/auth/login") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun register(request: GatewayUserRegisterRequest): AuthResult =
    httpClient.post("${GatewayConfig.baseUrl}/auth/register") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun fetchCurrentUser(): UserInfoDto {
    val token = AuthToken.accessToken
        ?: throw IllegalStateException("Not authenticated")
    return httpClient.get("${GatewayConfig.baseUrl}/users/me") {
        header(HttpHeaders.Authorization, "Bearer $token")
    }.body()
}

suspend fun fetchUserBids(
    status: String? = null,
    page: Int = 0,
    size: Int = DEFAULT_PAGE_SIZE,
): UserBidPage {
    val token = AuthToken.accessToken
        ?: throw IllegalStateException("Not authenticated")
    return httpClient.get("${GatewayConfig.baseUrl}/users/me/bids") {
        header(HttpHeaders.Authorization, "Bearer $token")
        parameter("page", page)
        parameter("size", size)
        if (status != null) parameter("status", status)
    }.body()
}

suspend fun fetchUserWatchlist(): List<LotSummary> {
    val token = AuthToken.accessToken
        ?: throw IllegalStateException("Not authenticated")
    return httpClient.get("${GatewayConfig.baseUrl}/users/me/watchlist") {
        header(HttpHeaders.Authorization, "Bearer $token")
    }.body()
}

suspend fun addToWatchlist(lotId: String) {
    val token = AuthToken.accessToken
        ?: throw IllegalStateException("Not authenticated")
    httpClient.put("${GatewayConfig.baseUrl}/users/me/watchlist/$lotId") {
        header(HttpHeaders.Authorization, "Bearer $token")
    }
}

suspend fun removeFromWatchlist(lotId: String) {
    val token = AuthToken.accessToken
        ?: throw IllegalStateException("Not authenticated")
    httpClient.delete("${GatewayConfig.baseUrl}/users/me/watchlist/$lotId") {
        header(HttpHeaders.Authorization, "Bearer $token")
    }
}
