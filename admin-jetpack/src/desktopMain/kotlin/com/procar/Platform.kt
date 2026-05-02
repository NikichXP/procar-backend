package com.procar

import coil3.PlatformContext
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import java.util.prefs.Preferences

private val prefs: Preferences = Preferences.userRoot().node("com/procar/admin")

actual object TokenStorage {
    actual fun getItem(key: String): String? = prefs.get(key, null)
    actual fun setItem(key: String, value: String) = prefs.put(key, value)
    actual fun removeItem(key: String) = prefs.remove(key)
}

actual fun createHttpClient(): HttpClient = HttpClient(CIO)

actual fun getPlatformContext(): PlatformContext = PlatformContext.INSTANCE
