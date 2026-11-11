package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TelegramBlue,
    onPrimary = Color.White,
    primaryContainer = TelegramDarkOutgoingBubble,
    onPrimaryContainer = Color.White,
    secondary = TelegramBlueDark,
    onSecondary = Color.White,
    background = TelegramDarkBg,
    onBackground = TelegramDarkTextPrimary,
    surface = TelegramDarkSurface,
    onSurface = TelegramDarkTextPrimary,
    surfaceVariant = TelegramDarkIncomingBubble,
    onSurfaceVariant = TelegramDarkTextSecondary,
    outline = Color(0xFF243447),
    error = TelegramRed
)

private val LightColorScheme = lightColorScheme(
    primary = TelegramBlueDark,
    onPrimary = Color.White,
    primaryContainer = TelegramLightOutgoingBubble,
    onPrimaryContainer = TelegramLightTextPrimary,
    secondary = TelegramBlue,
    onSecondary = Color.White,
    background = TelegramLightBg,
    onBackground = TelegramLightTextPrimary,
    surface = TelegramLightSurface,
    onSurface = TelegramLightTextPrimary,
    surfaceVariant = TelegramLightIncomingBubble,
    onSurfaceVariant = TelegramLightTextSecondary,
    outline = Color(0xFFE0E0E0),
    error = TelegramRed
)

@Composable
fun TeleChatTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
