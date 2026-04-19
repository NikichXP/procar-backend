package com.procar.model

import kotlinx.serialization.Serializable

// --- Lot models ---

@Serializable
data class AdminLotResponse(
    val id: String,
    val externalId: String,
    val title: String,
    val description: String,
    val vehicle: AdminVehicleInfoResponse,
    val auction: AdminAuctionInfoResponse,
    val location: AdminLocationInfoResponse,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
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
    val auctionType: String,
    val totalBids: Int = 0,
    val reservePrice: Double? = null,
    val buyItNowPrice: Double? = null,
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

@Serializable
data class AdminCreateLotRequest(
    val externalId: String,
    val title: String,
    val description: String,
    val vehicle: AdminVehicleInfoRequest,
    val auction: AdminAuctionInfoRequest,
    val location: AdminLocationInfoRequest,
    val metadata: AdminLotMetadataRequest,
    val status: String,
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
    val auctionType: String,
    val reservePrice: Double? = null,
    val buyItNowPrice: Double? = null,
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

@Serializable
data class AdminLotMetadataRequest(
    val sellerInfo: AdminSellerInfoRequest,
)

@Serializable
data class AdminSellerInfoRequest(
    val id: String,
    val name: String,
    val type: String,
)

@Serializable
data class AdminUpdateLotRequest(
    val title: String? = null,
    val description: String? = null,
    val vehicle: AdminVehicleInfoRequest? = null,
    val auction: AdminAuctionInfoRequest? = null,
    val location: AdminLocationInfoRequest? = null,
    val metadata: AdminLotMetadataRequest? = null,
    val status: String? = null,
)

@Serializable
data class AdminPaginatedLotsResponse(
    val lots: List<AdminLotResponse>,
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

// --- User models (local only, no API yet) ---

data class AdminUser(
    val id: String,
    val username: String,
    val email: String,
    val status: UserStatus,
    val createdAt: String,
    val lastLogin: String?,
)

enum class UserStatus { ACTIVE, BANNED, PENDING }

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
