package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PolishPrimaryDark,
    onPrimary = PolishOnPrimaryDark,
    primaryContainer = PolishPrimaryContainerDark,
    onPrimaryContainer = PolishOnPrimaryContainerDark,
    background = PolishBackgroundDark,
    onBackground = PolishOnBackgroundDark,
    surface = PolishSurfaceDark,
    onSurface = PolishOnSurfaceDark,
    surfaceVariant = PolishSurfaceVariantDark,
    onSurfaceVariant = PolishOnSurfaceVariantDark,
    outline = PolishBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = PolishPrimary,
    onPrimary = PolishOnPrimary,
    primaryContainer = PolishPrimaryContainer,
    onPrimaryContainer = PolishOnPrimaryContainer,
    background = PolishBackground,
    onBackground = PolishOnBackground,
    surface = PolishSurface,
    onSurface = PolishOnSurface,
    surfaceVariant = PolishSurfaceVariant,
    onSurfaceVariant = PolishOnSurfaceVariant,
    outline = PolishBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce our custom developer branding
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
