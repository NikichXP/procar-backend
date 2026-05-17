package com.procar.user.converter

import com.procar.user.api.dto.BrokerDto
import com.procar.user.entity.BrokerEntity
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class BrokerEntityToBrokerDtoConverter : Converter<BrokerEntity, BrokerDto> {
    override fun convert(source: BrokerEntity): BrokerDto = BrokerDto(
        id = source.id,
        companyName = source.companyName,
        displayName = source.displayName,
        address = source.address,
        country = source.country,
        contactEmail = source.contactEmail,
        phones = source.phones,
        emails = source.emails,
        status = source.status
    )
}
