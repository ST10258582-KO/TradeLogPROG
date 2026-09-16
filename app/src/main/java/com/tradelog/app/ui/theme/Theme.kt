package com.tradelog.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// TradeLog dark colour scheme — Midnight Dark theme (Part 1 Planning Document Section 4.1)
// Uses Material Design 3 darkColorScheme — Reference: Google LLC, "Material Design 3" (2024)
private val TradeLogDarkColorScheme = darkColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = Color(0xFFE9DDFF),
    secondary = NeonGreen,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1A3300),
    onSecondaryContainer = NeonGreen,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSecondary,
    outline = DividerColor,
    error = LossRed,
    onError = Color.White
)

@Composable
fun TradeLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TradeLogDarkColorScheme,
        typography = TradeLogTypography,
        content = content
    )
}
