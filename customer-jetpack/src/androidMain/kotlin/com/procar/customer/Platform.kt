package com.procar.customer

import android.content.Context
import coil3.PlatformContext
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*

private var appContext: Context? = null

fun initPlatform(context: Context) {
    appContext = context.applicationContext
}

actual fun createHttpClient(): HttpClient = HttpClient(OkHttp)

actual fun getPlatformContext(): PlatformContext =
    appContext ?: throw IllegalStateException("Platform not initialized — call initPlatform() in MainActivity")
