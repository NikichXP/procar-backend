package com.procar.auth.service

import com.procar.auth.dto.AuthResult
import com.procar.auth.entity.PasswordAuthReason
import com.procar.auth.repo.PasswordAuthRepository
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.util.Base64

@Service
class PasswordAuthService(
    private val authService: AuthService,
    private val mongoTemplate: MongoTemplate,
    private val passwordAuthRepository: PasswordAuthRepository
) {

    private val secureRandom = SecureRandom()
    private val iterations = 10000
    private val keyLength = 256
    private val algorithm = "PBKDF2WithHmacSHA256"

    fun authenticate(username: String, password: String): AuthResult {
        val authReason = passwordAuthRepository.findByUserId(username)

        return when {
            authReason != null && verifyPassword(password, authReason.salt, authReason.passwordHash) -> {
                val refreshToken = authService.generateRefreshToken(authReason)
                val accessToken = authService.generateAccessToken(authReason)
                AuthResult(
                    success = true,
                    message = "Authentication successful",
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
            }
            else -> {
                AuthResult(
                    success = false,
                    message = "Invalid username or password"
                )
            }
        }
    }

    fun registerUser(userId: String, username: String, password: String): PasswordAuthReason {
        val salt = Base64.getEncoder().encodeToString(generateSalt())
        val passwordHash = hashPassword(password, salt)
        val authReason = PasswordAuthReason(
            id = username, // Use username as ID for lookup
            userId = userId,
            salt = salt,
            passwordHash = passwordHash
        )
        return passwordAuthRepository.save(authReason)
    }

    fun updatePassword(userId: String, username: String, oldPassword: String, newPassword: String): Boolean {
        val existingAuth = passwordAuthRepository.findByUserId(username)
        
        return if (existingAuth != null && existingAuth.userId == userId && 
                   verifyPassword(oldPassword, existingAuth.salt, existingAuth.passwordHash)) {
            val newSalt = Base64.getEncoder().encodeToString(generateSalt())
            val newPasswordHash = hashPassword(newPassword, newSalt)
            val updatedAuth = existingAuth.copy(
                salt = newSalt,
                passwordHash = newPasswordHash
            )
            passwordAuthRepository.save(updatedAuth)
            true
        } else {
            false
        }
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        secureRandom.nextBytes(salt)
        return salt
    }

    private fun hashPassword(password: String, salt: String): String {
        val saltBytes = Base64.getDecoder().decode(salt)
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), saltBytes, iterations, keyLength)
        val factory = SecretKeyFactory.getInstance(algorithm)
        val hash = factory.generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(hash)
    }

    private fun verifyPassword(password: String, salt: String, storedHash: String): Boolean {
        val computedHash = hashPassword(password, salt)
        return computedHash == storedHash
    }
}