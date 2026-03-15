package com.procar.auction.document

import com.procar.provider.bid.BidStatus
import com.procar.provider.bid.BidType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document(collection = "bids")
data class BidDocument(
    @Id
    val id: String? = null,
    
    @Field("lot_id")
    val lotId: String,
    
    @Field("provider_id")
    val providerId: String = "procar",
    
    @Field("external_id")
    val externalId: String?,
    
    @Field("bidder_id")
    val bidderId: String,
    
    @Field("amount")
    val amount: Double,
    
    @Field("bid_type")
    val bidType: BidType,
    
    @Field("status")
    var status: BidStatus,
    
    @Field("is_winning")
    var isWinning: Boolean,
    
    @Field("is_auto_bid")
    val isAutoBid: Boolean,
    
    @Field("placed_at")
    val placedAt: LocalDateTime = LocalDateTime.now()
)
