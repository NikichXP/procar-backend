package com.procar.auction.repository

interface LotRepositoryCustom {
    fun lockForPayment(lotId: String): Boolean
}
