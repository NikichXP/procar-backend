package com.procar.auction.document

import com.procar.provider.bid.BidStatus
import com.procar.provider.bid.BidType
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Document(collection = "bids")
data class BidEntity(
    @Id
    @OptIn(ExperimentalUuidApi::class)
    val id: String = Uuid.generateV7().toString(),

    @Field("lot_id")
    val lotId: String,

    @Field("provider_id")
    val providerId: String = "procar",

    @Field("bidder_id")
    val bidderId: String,

    @Field("amount")
    val amount: Double,

    @Field("bid_type")
    val bidType: BidType,

    @Field("status")
    val status: BidStatus,

    @Field("is_auto_bid")
    val isAutoBid: Boolean,

    @Field("placed_at")
    val placedAt: LocalDateTime = LocalDateTime.now()
) {
    @Transient
    var isWinning: Boolean = false
}
