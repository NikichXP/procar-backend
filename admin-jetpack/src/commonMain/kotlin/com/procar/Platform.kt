package com.procar

import coil3.PlatformContext
import io.ktor.client.*

expect object TokenStorage {
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
    fun removeItem(key: String)
}

expect fun getPlatformContext(): PlatformContext

expect fun createHttpClient(): HttpClient
