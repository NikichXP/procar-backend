package com.procar.user.service

import com.procar.user.api.dto.BlockUserRequest
import com.procar.user.api.dto.CreateUserRequest
import com.procar.user.api.dto.UpdateUserBrokerRequest
import com.procar.user.api.dto.UpdateUserRolesRequest
import com.procar.user.api.dto.UserDto
import com.procar.user.api.dto.UserRole
import com.procar.user.entity.UserEntity
import com.procar.user.repo.UserRepository
import org.springframework.core.convert.ConversionService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    private val userRepository: UserRepository,
    private val brokerService: BrokerService,
    private val conversionService: ConversionService
) {

    private fun UserEntity.toDto(): UserDto =
        conversionService.convert(this, UserDto::class.java)!!

    private fun validateBrokerId(brokerOrgId: String?) {
        if (brokerOrgId != null && !brokerService.existsById(brokerOrgId)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Broker '$brokerOrgId' does not exist")
        }
    }

    suspend fun getUsers(): List<UserDto> {
        return userRepository.findAll().map { it.toDto() }
    }

    suspend fun createUser(request: CreateUserRequest): UserDto {
        validateBrokerId(request.brokerOrgId)
        val userEntity = UserEntity(
            username = request.username,
            brokerOrgId = request.brokerOrgId,
            roles = request.roles ?: listOf(UserRole.USER)
        )
        return userRepository.save(userEntity).toDto()
    }

    suspend fun getUser(id: String): UserDto? {
        return userRepository.findById(id)?.toDto()
    }

    suspend fun blockUser(id: String, request: BlockUserRequest): UserDto? {
        val user = userRepository.findById(id) ?: return null
        val updatedUser = user.copy(blocked = request.blocked)
        return userRepository.save(updatedUser).toDto()
    }

    suspend fun updateUserRoles(id: String, request: UpdateUserRolesRequest): UserDto? {
        val user = userRepository.findById(id) ?: return null
        val roles = request.roles.distinct().ifEmpty { listOf(UserRole.USER) }
        return userRepository.save(user.copy(roles = roles)).toDto()
    }

    suspend fun updateUserBroker(id: String, request: UpdateUserBrokerRequest): UserDto? {
        val user = userRepository.findById(id) ?: return null
        validateBrokerId(request.brokerOrgId)
        return userRepository.save(user.copy(brokerOrgId = request.brokerOrgId)).toDto()
    }

    suspend fun getUserByUsername(username: String): UserDto? {
        return userRepository.findByUsername(username)?.toDto()
    }
}
