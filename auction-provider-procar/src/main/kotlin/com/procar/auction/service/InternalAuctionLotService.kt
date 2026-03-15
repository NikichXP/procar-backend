package com.procar.auction.service

import com.procar.auction.api.admin.AdminLotResponse
import com.procar.auction.api.admin.AdminPaginatedLotsResponse
import com.procar.auction.document.LotDocument
import com.procar.auction.repository.LotRepository
import com.procar.provider.common.PaginationResponse
import com.procar.provider.common.SortDirection
import com.procar.provider.common.SortField
import com.procar.provider.lot.AdvancedLotSearchRequest
import com.procar.provider.lot.LotSearchResponse
import com.procar.provider.lot.LotStatus
import com.procar.provider.lot.ProviderLot
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class InternalAuctionLotService(
    private val lotRepository: LotRepository,
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
        val lots = when {
            !request.query.isNullOrBlank() -> {
                // For text search, we'll use a simple implementation for now
                lotRepository.findAll().filter { lot ->
                    request.query?.let { query ->
                        lot.title.contains(query, ignoreCase = true) ||
                        lot.description.contains(query, ignoreCase = true)
                    } ?: false
                }
            }
            else -> {
                // Apply filters
                var filteredLots = lotRepository.findAll()
                
                request.filters?.let { filters ->
                    filters.status?.let { statuses ->
                        filteredLots = filteredLots.filter { lot ->
                            lot.status in statuses
                        }
                    }
                    
                    filters.vehicle?.let { vehicle ->
                        vehicle.makes?.let { makes ->
                            filteredLots = filteredLots.filter { lot ->
                                lot.vehicle.make in makes
                            }
                        }
                        
                        vehicle.models?.let { models ->
                            filteredLots = filteredLots.filter { lot ->
                                lot.vehicle.model in models
                            }
                        }
                        
                        vehicle.bodyTypes?.let { bodyTypes ->
                            filteredLots = filteredLots.filter { lot ->
                                lot.vehicle.bodyType in bodyTypes
                            }
                        }
                    }
                    
                    filters.priceRange?.let { range ->
                        filteredLots = filteredLots.filter { lot ->
                            lot.auction.currentBid >= (range.min ?: 0.0) && 
                            lot.auction.currentBid <= (range.max ?: Double.MAX_VALUE)
                        }
                    }
                    
                    filters.mileageRange?.let { range ->
                        filteredLots = filteredLots.filter { lot ->
                            (lot.vehicle.mileage ?: 0) >= range.first && 
                            (lot.vehicle.mileage ?: Int.MAX_VALUE) <= range.last
                        }
                    }
                    
                    filters.location?.let { location ->
                        filteredLots = filterByLocationField(filteredLots, location.cities) { lot -> lot.location.city }
                        filteredLots = filterByLocationField(filteredLots, location.states) { lot -> lot.location.state }
                    }
                }
                
                filteredLots
            }
        }

        // Apply sorting
        val sortedLots = if (request.sorting.isNotEmpty()) {
            val sortCriteria = request.sorting.first()
            lots.sortedWith(compareBy<LotDocument> { lot ->
                when (sortCriteria.field) {
                    SortField.END_TIME -> lot.auction.endTime
                    SortField.START_TIME -> lot.auction.startTime
                    SortField.CURRENT_BID -> lot.auction.currentBid
                    SortField.STARTING_BID -> lot.auction.startingBid
                    SortField.MILEAGE -> lot.vehicle.mileage ?: 0
                    SortField.YEAR -> lot.vehicle.year
                    SortField.MAKE -> lot.vehicle.make
                    SortField.MODEL -> lot.vehicle.model
                    SortField.LOCATION -> lot.location.city
                    else -> lot.auction.endTime
                }
            }.let { comparator ->
                if (sortCriteria.direction == SortDirection.DESC) {
                    comparator.reversed()
                } else {
                    comparator
                }
            })
        } else {
            lots
        }

        // Apply pagination
        val startIndex = if (request.pagination.cursor != null) {
            // Simple cursor implementation - in production, use proper cursor-based pagination
            (request.pagination.cursor.hashCode() % sortedLots.size).coerceAtLeast(0)
        } else {
            0
        }
        
        val endIndex = (startIndex + request.pagination.size).coerceAtMost(sortedLots.size)
        val paginatedLots = sortedLots.subList(startIndex, endIndex)

        val providerLots = paginatedLots.mapNotNull { conversionService.convert(it, ProviderLot::class.java) }
        
        return LotSearchResponse(
            results = providerLots,
            pagination = PaginationResponse(
                hasNext = endIndex < sortedLots.size,
                nextCursor = if (endIndex < sortedLots.size) "cursor-$endIndex" else null
            ),
            suggestions = null,
            correctedQuery = null
        )
    }
    
    private fun filterByLocationField(
        lots: List<LotDocument>, 
        fieldValues: Collection<String>?, 
        extractor: (LotDocument) -> String
    ): List<LotDocument> {
        return fieldValues?.let { values ->
            lots.filter { lot -> extractor(lot) in values }
        } ?: lots
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
            AdminLotResponse.fromLotDocument(lot)
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
