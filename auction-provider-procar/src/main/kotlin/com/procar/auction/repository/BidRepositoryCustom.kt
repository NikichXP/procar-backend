package com.procar.auction.repository

interface BidRepositoryCustom {
    fun advanceBid(lotId: String, amount: Double, closeOnWin: Boolean): Boolean
}
