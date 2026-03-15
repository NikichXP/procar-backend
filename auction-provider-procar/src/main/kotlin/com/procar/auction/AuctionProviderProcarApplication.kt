package com.procar.auction

import com.procar.auction.config.AuctionProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(AuctionProperties::class)
class AuctionProviderProcarApplication

fun main(args: Array<String>) {
    runApplication<AuctionProviderProcarApplication>(*args)
}
