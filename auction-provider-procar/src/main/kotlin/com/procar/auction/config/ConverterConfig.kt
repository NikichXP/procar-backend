package com.procar.auction.config

import com.procar.auction.converter.*
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class ConverterConfig(
    private val lotDocumentToProviderLotConverter: LotDocumentToProviderLotConverter,
    private val bidDocumentToProviderBidConverter: BidDocumentToProviderBidConverter,
    private val adminCreateLotRequestToLotDocumentConverter: AdminCreateLotRequestToLotDocumentConverter,
    private val adminUpdateLotRequestToLotDocumentConverter: AdminUpdateLotRequestToLotDocumentConverter,
    private val lotDocumentToAdminLotResponseConverter: LotDocumentToAdminLotResponseConverter
) : WebFluxConfigurer {

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(lotDocumentToProviderLotConverter)
        registry.addConverter(bidDocumentToProviderBidConverter)
        registry.addConverter(adminCreateLotRequestToLotDocumentConverter)
        registry.addConverter(adminUpdateLotRequestToLotDocumentConverter)
        registry.addConverter(lotDocumentToAdminLotResponseConverter)
    }
}
