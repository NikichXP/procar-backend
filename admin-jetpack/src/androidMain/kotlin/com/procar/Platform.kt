package com.procar

import android.content.Context
import android.content.SharedPreferences
import coil3.PlatformContext
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*

actual object TokenStorage {
    private var prefs: SharedPreferences? = null
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        prefs = context.getSharedPreferences("procar_prefs", Context.MODE_PRIVATE)
    }

    actual fun getItem(key: String): String? = prefs?.getString(key, null)
    actual fun setItem(key: String, value: String) {
        prefs?.edit()?.putString(key, value)?.apply()
    }
    actual fun removeItem(key: String) {
        prefs?.edit()?.remove(key)?.apply()
    }

    fun getContext(): Context = appContext ?: throw IllegalStateException("TokenStorage not initialized with context")
}

actual fun createHttpClient(): HttpClient = HttpClient(OkHttp)

actual fun getPlatformContext(): PlatformContext = TokenStorage.getContext()
