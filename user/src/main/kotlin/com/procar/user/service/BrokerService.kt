package com.procar.user.service

import com.procar.user.api.dto.BrokerDto
import com.procar.user.api.dto.CreateBrokerRequest
import com.procar.user.api.dto.UpdateBrokerRequest
import com.procar.user.entity.BrokerEntity
import com.procar.user.repo.BrokerRepository
import org.springframework.core.convert.ConversionService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class BrokerService(
    private val brokerRepository: BrokerRepository,
    private val conversionService: ConversionService
) {

    private val idPattern = Regex("[a-z0-9\\-]+")

    private fun BrokerEntity.toDto(): BrokerDto =
        conversionService.convert(this, BrokerDto::class.java)!!

    fun getBrokers(): List<BrokerDto> = brokerRepository.findAll().map { it.toDto() }

    fun getBroker(id: String): BrokerDto? = brokerRepository.findById(id)?.toDto()

    fun createBroker(request: CreateBrokerRequest): BrokerDto {
        if (!idPattern.matches(request.id)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Broker id must match [a-z0-9\\-]+")
        }
        if (brokerRepository.existsById(request.id)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Broker with id '${request.id}' already exists")
        }
        val entity = BrokerEntity(
            id = request.id,
            name = request.name,
            address = request.address,
            phones = request.phones,
            emails = request.emails
        )
        return brokerRepository.save(entity).toDto()
    }

    fun updateBroker(id: String, request: UpdateBrokerRequest): BrokerDto? {
        val existing = brokerRepository.findById(id) ?: return null
        val updated = existing.copy(
            name = request.name ?: existing.name,
            address = request.address ?: existing.address,
            phones = request.phones ?: existing.phones,
            emails = request.emails ?: existing.emails,
            updatedAt = Instant.now()
        )
        return brokerRepository.save(updated).toDto()
    }

    fun deleteBroker(id: String): Boolean {
        if (!brokerRepository.existsById(id)) return false
        brokerRepository.deleteById(id)
        return true
    }

    fun existsById(id: String): Boolean = brokerRepository.existsById(id)
}
