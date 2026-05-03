package com.procar.customer

import coil3.PlatformContext
import io.ktor.client.*
import io.ktor.client.engine.cio.*

actual fun createHttpClient(): HttpClient = HttpClient(CIO)

actual fun getPlatformContext(): PlatformContext = PlatformContext.INSTANCE
