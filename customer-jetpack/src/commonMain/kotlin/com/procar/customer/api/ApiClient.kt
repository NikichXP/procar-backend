package com.procar.customer.api

import coil3.ImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.procar.customer.createHttpClient
import com.procar.customer.getPlatformContext
import com.procar.gateway.api.dto.CarCondition
import com.procar.gateway.api.dto.LotDetail
import com.procar.gateway.api.dto.LotPage
import com.procar.gateway.api.dto.LotStatus
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object GatewayConfig {
    var baseUrl: String = "http://localhost:8080"
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
): LotPage = httpClient.get("${GatewayConfig.baseUrl}/lots") {
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
    httpClient.get("${GatewayConfig.baseUrl}/lots/$lotId").body()
