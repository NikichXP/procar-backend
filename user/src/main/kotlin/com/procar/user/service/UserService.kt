package com.procar.user.service

import com.procar.auth.api.AuthController
import com.procar.user.api.dto.*
import com.procar.user.entity.UserEntity
import com.procar.user.repo.UserRepository
import org.springframework.core.convert.ConversionService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val brokerService: BrokerService,
    private val conversionService: ConversionService,
    private val authController: AuthController
) {

    private fun UserEntity.toDto(): UserDto =
        conversionService.convert(this, UserDto::class.java)!!

    private fun validateBrokerId(brokerId: String?) {
        if (brokerId != null && !brokerService.existsById(brokerId)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Broker '$brokerId' does not exist")
        }
    }

    suspend fun getUsers(): List<UserDto> {
        return userRepository.findAll().map { it.toDto() }
    }

    suspend fun createUser(request: CreateUserRequest): UserDto {
        validateBrokerId(request.brokerId)
        val userEntity = UserEntity(
            username = request.username,
            roles = request.roles,
            brokerId = request.brokerId,
            companyName = request.companyName,
            country = request.country
        )
        return userRepository.save(userEntity).toDto()
    }

    suspend fun getUser(id: String): UserDto? {
        return userRepository.findById(id)?.toDto()
    }

    suspend fun deleteUser(id: String) {
        val user = userRepository.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User '$id' not found")
        
        // Delete auth records first
        try {
            authController.deleteAuthRecords(id)
        } catch (e: Exception) {
            // Log warning but continue with user deletion
        }
        
        userRepository.deleteById(id)
    }

    suspend fun updateUserStatus(id: String, status: UserStatus): UserDto? {
        val user = userRepository.findById(id) ?: return null
        user.status = status
        user.updatedAt = LocalDateTime.now()
        return userRepository.save(user).toDto()
    }

    suspend fun patchUser(id: String, request: PatchUserRequest): UserDto? {
        val user = userRepository.findById(id) ?: return null
        
        if (request.brokerId != null) {
            validateBrokerId(request.brokerId)
        }

        user.apply {
            request.username?.let { username = it }
            request.roles?.let { roles = it }
            if (request.brokerId !== null) brokerId = request.brokerId
            if (request.companyName !== null) companyName = request.companyName
            if (request.country !== null) country = request.country
            request.status?.let { status = it }
            request.verificationStatus?.let { verificationStatus = it }
            request.depositStatus?.let { depositStatus = it }
            // TODO: create an update service that will update those trackable entities and autoupdate updatedAt
            updatedAt = LocalDateTime.now()
        }
        
        return userRepository.save(user).toDto()
    }

    suspend fun getUserByUsername(username: String): UserDto? {
        return userRepository.findByUsername(username)?.toDto()
    }
}
