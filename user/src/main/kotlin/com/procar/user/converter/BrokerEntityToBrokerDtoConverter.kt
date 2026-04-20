package com.procar.user.converter

import com.procar.user.api.dto.BrokerDto
import com.procar.user.entity.BrokerEntity
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class BrokerEntityToBrokerDtoConverter : Converter<BrokerEntity, BrokerDto> {
    override fun convert(source: BrokerEntity): BrokerDto = BrokerDto(
        id = source.id,
        name = source.name,
        address = source.address,
        phones = source.phones,
        emails = source.emails
    )
}
