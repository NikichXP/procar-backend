package com.procar.customer

import coil3.PlatformContext
import io.ktor.client.*
import io.ktor.client.engine.js.*

actual fun createHttpClient(): HttpClient = HttpClient(Js)

actual fun getPlatformContext(): PlatformContext = PlatformContext.INSTANCE
