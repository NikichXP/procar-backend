package com.procar.api

import coil3.ImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.procar.TokenStorage
import com.procar.createHttpClient
import com.procar.getPlatformContext
import com.procar.gateway.api.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
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
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
        install(Auth) {
            bearer {
                loadTokens {
                    val refresh = AuthState.refreshToken
                    if (refresh.isEmpty()) null
                    else BearerTokens(AuthState.accessToken, refresh)
                }
                refreshTokens {
                    val refresh = AuthState.refreshToken
                    if (refresh.isEmpty()) return@refreshTokens null
                    try {
                        val newToken: AccessToken = client.post("$GATEWAY_BASE_URL/auth/access") {
                            markAsRefreshTokenRequest()
                            parameter("refreshToken", refresh)
                        }.body()
                        AuthState.accessToken = newToken.token
                        BearerTokens(newToken.token, refresh)
                    } catch (_: Exception) {
                        // Refresh token is invalid/expired -> force re-login.
                        AuthState.accessToken = ""
                        AuthState.refreshToken = ""
                        null
                    }
                }
                // Attach Bearer proactively for every non-auth request
                // (avoids the extra 401 round-trip on each call).
                sendWithoutRequest { request ->
                    !request.url.encodedPath.startsWith("/auth/")
                }
            }
        }
    }
}

private suspend inline fun <reified T> getEnveloped(
    url: String,
    block: HttpRequestBuilder.() -> Unit = {},
): T = httpClient.get(url, block).body<ApiResponse<T>>().data

private suspend inline fun <reified T> postEnveloped(url: String, body: Any): T =
    httpClient.post(url) {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(body)
    }.body<ApiResponse<T>>().data

private suspend inline fun <reified T> putEnveloped(url: String, body: Any): T =
    httpClient.put(url) {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(body)
    }.body<ApiResponse<T>>().data

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

const val DEFAULT_LOTS_PAGE_SIZE: Int = 25

suspend fun fetchLots(
    cursor: String? = null,
    limit: Int = DEFAULT_LOTS_PAGE_SIZE,
): AdminPaginatedLotsResponse =
    getEnveloped("$GATEWAY_BASE_URL/api/admin/lots") {
        parameter("limit", limit)
        if (cursor != null) parameter("cursor", cursor)
    }

suspend fun createLot(request: AdminCreateLotRequest): AdminLotResponse =
    postEnveloped("$GATEWAY_BASE_URL/api/admin/lots", request)

suspend fun updateLot(lotId: String, request: AdminUpdateLotRequest): AdminLotResponse =
    putEnveloped("$GATEWAY_BASE_URL/api/admin/lots/$lotId", request)

suspend fun updateLotStatus(lotId: String, status: AdminLotStatus): AdminLotResponse =
    postEnveloped("$GATEWAY_BASE_URL/api/admin/lots/$lotId/status", AdminUpdateStatusRequest(status))

suspend fun fetchPossibleStatuses(lotId: String): List<String> =
    getEnveloped("$GATEWAY_BASE_URL/api/admin/lots/$lotId/possible-statuses")

suspend fun uploadLotPhoto(
    lotId: String,
    fileName: String,
    contentType: String,
    bytes: ByteArray,
): AdminLotResponse =
    httpClient.post("$GATEWAY_BASE_URL/api/admin/lots/$lotId/photo") {
        setBody(MultiPartFormDataContent(formData {
            append("file", bytes, Headers.build {
                append(HttpHeaders.ContentType, contentType)
                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
            })
        }))
    }.body<ApiResponse<AdminLotResponse>>().data

suspend fun deleteLotPhoto(lotId: String, url: String): AdminLotResponse =
    httpClient.delete("$GATEWAY_BASE_URL/api/admin/lots/$lotId/images") {
        parameter("url", url)
    }.body<ApiResponse<AdminLotResponse>>().data

/**
 * Shared Coil3 ImageLoader that reuses the authed Ktor client, so protected
 * `files/{key}` URLs can be rendered inline via AsyncImage.
 */
val imageLoader: ImageLoader by lazy {
    ImageLoader.Builder(getPlatformContext())
        .components { add(KtorNetworkFetcherFactory(httpClient)) }
        .build()
}

fun absoluteImageUrl(url: String): String = when {
    url.startsWith("http://") || url.startsWith("https://") -> url
    url.startsWith("/") -> "$GATEWAY_BASE_URL$url"
    else -> "$GATEWAY_BASE_URL/files/$url"
}

suspend fun fetchWarehouses(): List<AdminWarehouseResponse> =
    getEnveloped("$GATEWAY_BASE_URL/api/admin/warehouses")

suspend fun createWarehouse(request: AdminCreateWarehouseRequest): AdminWarehouseResponse =
    postEnveloped("$GATEWAY_BASE_URL/api/admin/warehouses", request)

// --- Users ---

suspend fun fetchUsers(): List<UserDto> =
    httpClient.get("$GATEWAY_BASE_URL/api/admin/users").body()

suspend fun createUser(request: CreateUserRequest): UserDto =
    httpClient.post("$GATEWAY_BASE_URL/api/admin/users") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun blockUser(id: String, blocked: Boolean): UserDto =
    httpClient.post("$GATEWAY_BASE_URL/api/admin/users/$id/block") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(BlockUserRequest(blocked))
    }.body()

suspend fun updateUserRoles(id: String, roles: List<UserRole>): UserDto =
    httpClient.post("$GATEWAY_BASE_URL/api/admin/users/$id/roles") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(UpdateUserRolesRequest(roles))
    }.body()

suspend fun updateUserBroker(id: String, brokerOrgId: String?): UserDto =
    httpClient.post("$GATEWAY_BASE_URL/api/admin/users/$id/broker") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(UpdateUserBrokerRequest(brokerOrgId))
    }.body()

// --- Brokers ---

suspend fun fetchBrokers(): List<BrokerDto> =
    httpClient.get("$GATEWAY_BASE_URL/api/admin/brokers").body()

suspend fun createBroker(request: CreateBrokerRequest): BrokerDto =
    httpClient.post("$GATEWAY_BASE_URL/api/admin/brokers") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun updateBroker(id: String, request: UpdateBrokerRequest): BrokerDto =
    httpClient.put("$GATEWAY_BASE_URL/api/admin/brokers/$id") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun deleteBroker(id: String) {
    httpClient.delete("$GATEWAY_BASE_URL/api/admin/brokers/$id")
}
