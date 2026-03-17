package com.procar.auction.config

import com.procar.auction.converter.*
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class ConverterConfig : WebMvcConfigurer {

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(LotDocumentToProviderLotConverter())
        registry.addConverter(BidDocumentToProviderBidConverter())
        registry.addConverter(AdminCreateLotRequestToLotDocumentConverter())
        registry.addConverter(AdminUpdateLotRequestToLotDocumentConverter())
        registry.addConverter(LotDocumentToAdminLotResponseConverter())
    }
}
