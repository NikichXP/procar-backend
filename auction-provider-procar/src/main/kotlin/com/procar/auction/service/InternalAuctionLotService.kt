package com.procar.auction.service

import com.procar.provider.admin.AdminLotResponse
import com.procar.provider.admin.AdminPaginatedLotsResponse
import com.procar.auction.document.LotDocument
import com.procar.auction.repository.LotRepository
import com.procar.provider.common.PaginationResponse
import com.procar.provider.lot.AdvancedLotSearchRequest
import com.procar.provider.lot.LotSearchResponse
import com.procar.provider.lot.LotStatus
import com.procar.provider.lot.VehicleLot
import org.springframework.core.convert.ConversionService
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class InternalAuctionLotService(
    private val lotRepository: LotRepository,
    private val mongoTemplate: MongoTemplate,
    private val conversionService: ConversionService
) {

    fun createLot(lotDocument: LotDocument): LotDocument {
        val savedLot = lotRepository.save(lotDocument)
        
        return savedLot
    }

    fun getLotById(lotId: String): LotDocument? {
        return lotRepository.findById(lotId).orElse(null)
    }

    fun searchLots(request: AdvancedLotSearchRequest): LotSearchResponse {
        val criteria = mutableListOf<Criteria>()

        val query = request.query
        if (!query.isNullOrBlank()) {
            criteria.add(
                Criteria().orOperator(
                    Criteria.where("title").regex(query, "i"),
                    Criteria.where("description").regex(query, "i")
                )
            )
        }

        request.filters?.let { filters ->
            filters.status?.let { statuses ->
                criteria.add(Criteria.where("status").`in`(statuses.map { it.name }))
            }
            filters.priceRange?.let { range ->
                val priceCriteria = Criteria.where("auction.current_bid")
                range.min?.let { priceCriteria.gte(it) }
                range.max?.let { priceCriteria.lte(it) }
                criteria.add(priceCriteria)
            }
            filters.yearRange?.let { range ->
                criteria.add(Criteria.where("vehicle.year").gte(range.first).lte(range.last))
            }
            filters.mileageRange?.let { range ->
                criteria.add(Criteria.where("vehicle.mileage").gte(range.first).lte(range.last))
            }
            filters.vehicle?.let { v ->
                v.makes?.let { criteria.add(Criteria.where("vehicle.make").`in`(it)) }
                v.models?.let { criteria.add(Criteria.where("vehicle.model").`in`(it)) }
                v.bodyTypes?.let { criteria.add(Criteria.where("vehicle.body_type").`in`(it)) }
                v.fuelTypes?.let { criteria.add(Criteria.where("vehicle.fuel_type").`in`(it.map { t -> t.name })) }
                v.transmissions?.let { criteria.add(Criteria.where("vehicle.transmission").`in`(it.map { t -> t.name })) }
                v.drivetrains?.let { criteria.add(Criteria.where("vehicle.drivetrain").`in`(it.map { t -> t.name })) }
                v.colors?.let { criteria.add(Criteria.where("vehicle.color").`in`(it)) }
                v.hasDamage?.let { criteria.add(if (it) Criteria.where("vehicle.damage").exists(true).not().size(0) else Criteria.where("vehicle.damage").`is`(null).orOperator(Criteria.where("vehicle.damage").size(0))) }
            }
            filters.conditions?.let { criteria.add(Criteria.where("vehicle.condition").`in`(it.map { c -> c.name })) }
            filters.location?.let { loc ->
                loc.cities?.let { criteria.add(Criteria.where("location.city").`in`(it)) }
                loc.states?.let { criteria.add(Criteria.where("location.state").`in`(it)) }
                loc.zipCodes?.let { criteria.add(Criteria.where("location.zip_code").`in`(it)) }
            }
            filters.tags?.let { criteria.add(Criteria.where("metadata.tags").all(it)) }
            filters.categories?.let { criteria.add(Criteria.where("metadata.categories").all(it)) }
            filters.titleStatus?.let { criteria.add(Criteria.where("metadata.history.title_status").`in`(it.map { s -> s.name })) }
        }

        request.pagination.cursor?.let { cursor ->
            criteria.add(Criteria.where("_id").gt(cursor))
        }

        val mongoQuery = if (criteria.isNotEmpty()) {
            Query(Criteria().andOperator(*criteria.toTypedArray()))
        } else {
            Query()
        }

        val limit = request.pagination.limit
        mongoQuery.limit(limit + 1)
        val lots = mongoTemplate.find(mongoQuery, LotDocument::class.java)

        val hasNext = lots.size > limit
        val page = if (hasNext) lots.dropLast(1) else lots

        val providerLots = page.mapNotNull { conversionService.convert(it, VehicleLot::class.java) }

        return LotSearchResponse(
            results = providerLots,
            pagination = PaginationResponse(
                hasNext = hasNext,
                nextCursor = if (hasNext) page.last().id else null
            ),
            suggestions = null,
            correctedQuery = null
        )
    }

    fun updateLot(lotId: String, lotDocument: LotDocument): LotDocument {
        val existingLot = lotRepository.findById(lotId)
            .orElseThrow { IllegalArgumentException("Lot not found with id: $lotId") }
        
        val updatedLot = existingLot.copy(
            title = lotDocument.title,
            description = lotDocument.description,
            vehicle = lotDocument.vehicle,
            auction = lotDocument.auction,
            location = lotDocument.location,
            metadata = lotDocument.metadata,
            status = lotDocument.status,
            updatedAt = LocalDateTime.now()
        )
        
        return lotRepository.save(updatedLot)
    }

    fun deleteLot(lotId: String): Boolean {
        return if (lotRepository.existsById(lotId)) {
            lotRepository.deleteById(lotId)
            true
        } else {
            false
        }
    }

    fun updateLotStatus(lotId: String, status: LotStatus): LotDocument? {
        val existingLot = lotRepository.findById(lotId).orElse(null) ?: return null
        
        val updatedLot = existingLot.copy(
            status = status,
            updatedAt = LocalDateTime.now()
        )
        
        return lotRepository.save(updatedLot)
    }

    fun getAllLots(cursor: String?, limit: Int, status: LotStatus?): AdminPaginatedLotsResponse {
        // Parse cursor to get starting position (simple implementation)
        val startPosition = cursor?.let { 
            try {
                it.substringAfter("cursor-").toInt()
            } catch (e: Exception) {
                0
            }
        } ?: 0
        
        // Get all lots (in production, use proper cursor-based query)
        val allLots = when (status) {
            null -> lotRepository.findAll()
            else -> lotRepository.findByStatus(status)
        }
        
        // Sort by creation date for consistent pagination
        val sortedLots = allLots.sortedByDescending { it.createdAt }
        
        // Apply cursor and limit
        val paginatedLots = sortedLots.drop(startPosition).take(limit + 1) // +1 to check if there are more
        
        val hasMore = paginatedLots.size > limit
        val lotsToReturn = if (hasMore) paginatedLots.dropLast(1) else paginatedLots
        
        val adminLots = lotsToReturn.map { lot ->
            conversionService.convert(lot, AdminLotResponse::class.java)!!
        }
        
        val nextCursor = if (hasMore) {
            "cursor-${startPosition + limit}"
        } else {
            null
        }
        
        return AdminPaginatedLotsResponse(
            lots = adminLots,
            pagination = PaginationResponse(
                hasNext = hasMore,
                nextCursor = nextCursor
            )
        )
    }

    fun archiveLot(lotId: String): LotDocument? {
        return updateLotStatus(lotId, LotStatus.CANCELLED)
    }

    fun unarchiveLot(lotId: String): LotDocument? {
        return updateLotStatus(lotId, LotStatus.ACTIVE)
    }
}
