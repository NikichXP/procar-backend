package com.procar.auction

import com.procar.auction.config.AuctionProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableConfigurationProperties(AuctionProperties::class)
@EnableScheduling
@EnableAsync
class AuctionProviderProcarApplication

fun main(args: Array<String>) {
    runApplication<AuctionProviderProcarApplication>(*args)
}
