package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PrepaiDarkColorScheme = darkColorScheme(
    primary = PrimaryAmber,
    onPrimary = Color(0xFF11141B),
    secondary = AccentTurquoise,
    onSecondary = Color(0xFF11141B),
    tertiary = AccentTurquoise,
    background = BgMain,
    onBackground = TextPrimary,
    surface = BgCard,
    onSurface = TextPrimary,
    surfaceVariant = BgSecondary,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    error = AccentCoral,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PrepaiDarkColorScheme,
        typography = Typography,
        content = content
    )
}
