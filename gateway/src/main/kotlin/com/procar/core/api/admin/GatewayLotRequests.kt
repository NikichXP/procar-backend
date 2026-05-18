package com.procar.core.api.admin

import com.fasterxml.jackson.annotation.JsonProperty
import com.procar.provider.admin.AdminAuctionInfoRequest
import com.procar.provider.admin.AdminFeeRequest
import com.procar.provider.admin.AdminInspectionInfoRequest
import com.procar.provider.admin.AdminShippingInfoRequest
import com.procar.provider.admin.AdminVehicleHistoryRequest
import com.procar.provider.admin.AdminVehicleInfoRequest
import com.procar.provider.lot.LotBrand
import com.procar.provider.lot.LotStatus
import com.procar.provider.lot.LotType
import jakarta.validation.Valid

data class GatewayCreateLotRequest(
    val brokerId: String? = null,

    val warehouseId: String? = null,

    val externalId: String,
    val title: String,
    val description: String,

    @field:Valid
    val vehicle: AdminVehicleInfoRequest,

    @field:Valid
    val auction: AdminAuctionInfoRequest? = null,

    @field:Valid
    val metadata: GatewayLotMetadataRequest = GatewayLotMetadataRequest(),

    val status: LotStatus,
    val lotType: LotType = LotType.AUCTION,
    val buyoutPrice: Double? = null,
    @JsonProperty("brand")
    val brand: LotBrand = LotBrand.PARTNER,
)

data class GatewayLotMetadataRequest(
    val tags: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val inspection: AdminInspectionInfoRequest? = null,
    val history: AdminVehicleHistoryRequest? = null,
    val fees: List<AdminFeeRequest> = emptyList(),
    val shipping: AdminShippingInfoRequest? = null,
)
