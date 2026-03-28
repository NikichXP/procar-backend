package com.procar.auction.service

import com.procar.auction.document.WarehouseDocument
import com.procar.auction.repository.WarehouseRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class WarehouseService(
    private val warehouseRepository: WarehouseRepository
) {

    fun getAllWarehouses(): List<WarehouseDocument> = warehouseRepository.findAll()

    fun getWarehouseById(id: String): WarehouseDocument? = warehouseRepository.findByIdOrNull(id)

    fun createWarehouse(warehouse: WarehouseDocument): WarehouseDocument = warehouseRepository.save(warehouse)

    fun updateWarehouse(id: String, updated: WarehouseDocument): WarehouseDocument? {
        warehouseRepository.findByIdOrNull(id) ?: return null
        return warehouseRepository.save(updated.copy(updatedAt = LocalDateTime.now()))
    }

    fun deleteWarehouse(id: String): Boolean {
        if (!warehouseRepository.existsById(id)) return false
        warehouseRepository.deleteById(id)
        return true
    }
}
