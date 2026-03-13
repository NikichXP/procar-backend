package com.procar.auth.service

import org.springframework.stereotype.Indexed

@Indexed
interface AccessTokenService {
    fun storeAccessToken(tokenData: AuthService.AccessTokenData)
    fun getAccessTokenData(accessToken: String): AuthService.AccessTokenData?
    fun validateAccessToken(accessToken: String): Boolean
    fun deleteAccessToken(accessToken: String)
}
