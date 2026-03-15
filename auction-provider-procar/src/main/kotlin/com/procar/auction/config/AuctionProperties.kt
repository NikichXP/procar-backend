package com.procar.auction.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auction")
data class AuctionProperties(
    val bid: BidProperties = BidProperties()
)

data class BidProperties(
    val minimumIncrement: Double = 50.0,
    val maximumIncrement: Double = 1000.0,
    val defaultIncrement: Double = 100.0,
    val maxAutoBidAmount: Double = 100000.0
)
