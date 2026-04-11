package com.procar

import io.ktor.client.*

expect object TokenStorage {
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
    fun removeItem(key: String)
}

expect fun createHttpClient(): HttpClient
