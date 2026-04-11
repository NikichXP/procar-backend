package com.procar

import io.ktor.client.*
import io.ktor.client.engine.js.*

external interface JsStorage {
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
    fun removeItem(key: String)
}

external val localStorage: JsStorage

actual object TokenStorage {
    actual fun getItem(key: String): String? = localStorage.getItem(key)
    actual fun setItem(key: String, value: String) = localStorage.setItem(key, value)
    actual fun removeItem(key: String) = localStorage.removeItem(key)
}

actual fun createHttpClient(): HttpClient = HttpClient(Js)
