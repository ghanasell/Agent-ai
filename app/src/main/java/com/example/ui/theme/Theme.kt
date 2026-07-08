package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CarbonDark,
    secondary = TerminalGreen,
    onSecondary = CarbonDark,
    tertiary = ElectricAmber,
    onTertiary = CarbonDark,
    background = CarbonDark,
    onBackground = TextPrimary,
    surface = SlateSurface,
    onSurface = TextPrimary,
    surfaceVariant = CharcoalVariant,
    onSurfaceVariant = TextSecondary,
    outline = CodeBorder
)

// We maintain the dark-terminal style for light mode as well to preserve the IDE/code aesthetic
private val LightColorScheme = lightColorScheme(
    primary = NeonCyan,
    onPrimary = CarbonDark,
    secondary = TerminalGreen,
    onSecondary = CarbonDark,
    tertiary = ElectricAmber,
    onTertiary = CarbonDark,
    background = CarbonDark,
    onBackground = TextPrimary,
    surface = SlateSurface,
    onSurface = TextPrimary,
    surfaceVariant = CharcoalVariant,
    onSurfaceVariant = TextSecondary,
    outline = CodeBorder
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
