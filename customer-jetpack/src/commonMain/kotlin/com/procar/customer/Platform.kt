package com.procar.customer

import coil3.PlatformContext
import io.ktor.client.*

expect fun getPlatformContext(): PlatformContext

expect fun createHttpClient(): HttpClient
