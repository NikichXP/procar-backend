package com.procar.auction.service.status

import com.procar.auction.document.LotEntity
import com.procar.provider.lot.LotStatus
import org.springframework.stereotype.Component

@Component
interface LotStatusUpdateTask {

    fun fromStatus(): LotStatus
    fun toStatus(): LotStatus
    fun execute(lotEntity: LotEntity)

}