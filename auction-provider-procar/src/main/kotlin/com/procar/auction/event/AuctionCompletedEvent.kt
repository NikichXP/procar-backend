package com.procar.auction.event

import org.springframework.context.ApplicationEvent

// TODO
class AuctionCompletedEvent(
    source: Any,
    val lotId: String,
    val winnerId: String,
    val winningAmount: Double
) : ApplicationEvent(source)
