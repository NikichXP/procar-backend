package com.procar.model

import kotlinx.serialization.Serializable

// --- Lot models ---

@Serializable
enum class LotStatus {
    DRAFT,
    PENDING,
    ACTIVE,
    CLOSED,
    CANCELLED
}

@Serializable
enum class LotType {
    AUCTION,
    BUYOUT,
    HYBRID
}

@Serializable
data class AdminLotResponse(
    val id: String,
    val externalId: String,
    val title: String,
    val description: String,
    val vehicle: AdminVehicleInfoResponse,
    val auction: AdminAuctionInfoResponse? = null,
    val location: AdminLocationInfoResponse,
    val status: LotStatus,
    val createdAt: String,
    val updatedAt: String,
    val lotType: LotType = LotType.AUCTION,
    val buyoutPrice: Double? = null,
)

@Serializable
data class AdminVehicleInfoResponse(
    val make: String,
    val model: String,
    val year: Int,
    val bodyType: String,
    val transmission: String,
    val fuelType: String,
    val condition: String,
    val vin: String? = null,
    val trim: String? = null,
    val color: String? = null,
    val interiorColor: String? = null,
    val mileage: Int? = null,
    val drivetrain: String? = null,
    val engine: AdminEngineInfoResponse? = null,
    val images: List<AdminVehicleImageResponse> = emptyList(),
)

@Serializable
data class AdminVehicleImageResponse(
    val url: String,
    val type: String,
    val description: String? = null,
    val isPrimary: Boolean = false,
)

@Serializable
data class AdminEngineInfoResponse(
    val type: String,
    val displacement: Double? = null,
    val cylinders: Int? = null,
    val horsepower: Int? = null,
    val torque: Int? = null,
)

@Serializable
data class AdminAuctionInfoResponse(
    val currentBid: Double,
    val startingBid: Double,
    val bidIncrement: Double,
    val startTime: String,
    val endTime: String,
    val totalBids: Int = 0,
    val reservePrice: Double? = null,
)

@Serializable
data class AdminLocationInfoResponse(
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val timezone: String,
)

/**
 * Gateway-facing create-lot payload. The gateway resolves [brokerId] and
 * [warehouseId] to full broker/warehouse data, so the client does not need to
 * pass seller info or location.
 */
@Serializable
data class AdminCreateLotRequest(
    val brokerId: String,
    val warehouseId: String,
    val externalId: String,
    val title: String,
    val description: String,
    val vehicle: AdminVehicleInfoRequest,
    val auction: AdminAuctionInfoRequest? = null,
    val metadata: AdminLotMetadataRequest = AdminLotMetadataRequest(),
    val status: LotStatus,
    val lotType: LotType = LotType.AUCTION,
    val buyoutPrice: Double? = null,
)

@Serializable
data class AdminVehicleInfoRequest(
    val make: String,
    val model: String,
    val year: Int,
    val bodyType: String,
    val transmission: String,
    val drivetrain: String,
    val fuelType: String,
    val condition: String,
    val engine: AdminEngineInfoRequest,
    val vin: String? = null,
    val trim: String? = null,
    val color: String? = null,
    val interiorColor: String? = null,
    val mileage: Int? = null,
)

@Serializable
data class AdminEngineInfoRequest(val type: String)

@Serializable
data class AdminAuctionInfoRequest(
    val currentBid: Double,
    val startingBid: Double,
    val bidIncrement: Double,
    val startTime: String,
    val endTime: String,
    val reservePrice: Double? = null,
)

@Serializable
data class AdminLocationInfoRequest(
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val timezone: String,
)

/**
 * Gateway-level lot metadata. Seller info is intentionally omitted: it is
 * derived from the referenced broker on the server side.
 */
@Serializable
data class AdminLotMetadataRequest(
    val tags: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
)

@Serializable
data class AdminUpdateLotRequest(
    val title: String? = null,
    val description: String? = null,
    val vehicle: AdminVehicleInfoRequest? = null,
    val auction: AdminAuctionInfoRequest? = null,
    val location: AdminLocationInfoRequest? = null,
    val metadata: AdminLotMetadataRequest? = null,
    val status: LotStatus? = null,
    val lotType: LotType? = null,
    val buyoutPrice: Double? = null,
)

@Serializable
data class AdminPaginatedLotsResponse(
    val lots: List<AdminLotResponse>,
    val pagination: PaginationResponse = PaginationResponse(hasNext = false),
)

@Serializable
data class PaginationResponse(
    val hasNext: Boolean,
    val nextCursor: String? = null,
)

// --- Warehouse models ---

@Serializable
data class AdminWarehouseResponse(
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val timezone: String,
    val createdAt: String,
    val updatedAt: String,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
)

@Serializable
data class AdminCreateWarehouseRequest(
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val timezone: String,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
)

// --- User models ---

enum class UserRole { USER, BROKER, ADMIN }

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val blocked: Boolean,
    val roles: List<UserRole>,
    val brokerOrgId: String? = null,
)

@Serializable
data class CreateUserRequest(
    val username: String,
    val brokerOrgId: String? = null,
    val roles: List<UserRole>? = null,
)

@Serializable
data class BlockUserRequest(val blocked: Boolean)

@Serializable
data class UpdateUserRolesRequest(val roles: List<UserRole>)

@Serializable
data class UpdateUserBrokerRequest(val brokerOrgId: String? = null)

// --- Broker models ---

@Serializable
data class BrokerDto(
    val id: String,
    val name: String,
    val address: String = "",
    val phones: List<String> = emptyList(),
    val emails: List<String> = emptyList(),
)

@Serializable
data class CreateBrokerRequest(
    val id: String,
    val name: String,
    val address: String = "",
    val phones: List<String> = emptyList(),
    val emails: List<String> = emptyList(),
)

@Serializable
data class UpdateBrokerRequest(
    val name: String? = null,
    val address: String? = null,
    val phones: List<String>? = null,
    val emails: List<String>? = null,
)

// --- Auth models ---

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class AccessToken(val token: String, val validUntil: String)

@Serializable
data class AuthResult(
    val success: Boolean,
    val message: String? = null,
    val accessToken: AccessToken? = null,
    val refreshToken: String? = null,
)

// --- API wrapper ---

@Serializable
data class ApiResponse<T>(val data: T)
