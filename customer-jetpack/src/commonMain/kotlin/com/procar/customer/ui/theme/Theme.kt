package com.procar.customer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val primaryBlue = Color(0xFF1565C0)
private val primaryBlueDark = Color(0xFF003C8F)
private val primaryBlueLight = Color(0xFF5E92F3)
private val secondaryTeal = Color(0xFF00897B)
private val secondaryTealLight = Color(0xFF4EBAAA)
private val surfaceWhite = Color(0xFFFFFFFF)
private val backgroundGray = Color(0xFFF4F6F9)
private val onPrimaryWhite = Color(0xFFFFFFFF)
private val errorRed = Color(0xFFD32F2F)

val CustomerLightColorScheme = lightColorScheme(
    primary = primaryBlue,
    onPrimary = onPrimaryWhite,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001945),
    secondary = secondaryTeal,
    onSecondary = onPrimaryWhite,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF003731),
    background = backgroundGray,
    onBackground = Color(0xFF1A1C1E),
    surface = surfaceWhite,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE0E7F4),
    onSurfaceVariant = Color(0xFF44474E),
    error = errorRed,
    onError = onPrimaryWhite,
    outline = Color(0xFF74777F),
)

@Composable
fun CustomerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CustomerLightColorScheme,
        content = content,
    )
}
