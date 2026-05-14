package com.procar.user.service

import com.procar.auth.api.AuthController
import com.procar.auth.api.dto.RegisterRequest
import com.procar.user.api.dto.CreateUserRequest
import com.procar.user.api.dto.UserRole
import com.procar.user.repo.UserRepository
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Service
@EnableConfigurationProperties(DefaultUserProperties::class)
class DefaultUserInitializer(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val authController: AuthController,
    private val properties: DefaultUserProperties
) {
    private val logger = LoggerFactory.getLogger(DefaultUserInitializer::class.java)
    private val scope = CoroutineScope(Dispatchers.IO)

    @PostConstruct
    fun init() {
        scope.launch {
            try {
                initializeDefaultUser()
            } catch (e: Exception) {
                logger.error("Failed to initialize default user", e)
            }
        }
    }

    private suspend fun initializeDefaultUser() {
        var retries = 0
        val maxRetries = 10
        val delay = 5.seconds

        while (retries < maxRetries) {
            try {
                val users = userRepository.findAll()
                if (users.isEmpty()) {
                    logger.info("No users found in database. Creating default admin user.")
                    
                    val createUserRequest = CreateUserRequest(
                        username = properties.username,
                        roles = listOf(UserRole.ADMIN)
                    )
                    val userDto = userService.createUser(createUserRequest)
                    
                    authController.register(
                        RegisterRequest(
                            userId = userDto.id,
                            login = properties.username,
                            password = properties.password
                        )
                    )
                    logger.info("Default admin user '${properties.username}' created successfully.")
                } else {
                    logger.info("Users already exist in database. Skipping default user creation.")
                }
                return
            } catch (e: Exception) {
                retries++
                logger.warn("Attempt $retries failed to initialize default user: ${e.message}. Retrying in ${delay/1000}s...")
                delay(delay)
            }
        }
        logger.error("Failed to initialize default user after $maxRetries attempts.")
    }
}

@ConfigurationProperties(prefix = "procar.default-user")
data class DefaultUserProperties(
    val username: String = "admin",
    val password: String = "admin"
)
