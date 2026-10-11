package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NeonColorScheme = darkColorScheme(
    primary = NeonPrimary,
    onPrimary = NeonOnPrimary,
    primaryContainer = NeonPrimary.copy(alpha = 0.25f),
    onPrimaryContainer = NeonPrimary,
    secondary = NeonSecondary,
    onSecondary = NeonOnSecondary,
    secondaryContainer = NeonSecondary.copy(alpha = 0.2f),
    onSecondaryContainer = NeonSecondary,
    tertiary = NeonTertiary,
    onTertiary = NeonOnTertiary,
    background = NeonBackground,
    onBackground = NeonOnBackground,
    surface = NeonSurface,
    onSurface = NeonOnSurface,
    surfaceVariant = NeonSurfaceVariant,
    onSurfaceVariant = NeonOnSurfaceVariant,
    error = ErrorRed
)

private val OceanColorScheme = darkColorScheme(
    primary = OceanPrimary,
    onPrimary = OceanOnPrimary,
    primaryContainer = OceanPrimary.copy(alpha = 0.25f),
    onPrimaryContainer = OceanPrimary,
    secondary = OceanSecondary,
    onSecondary = OceanOnSecondary,
    secondaryContainer = OceanSecondary.copy(alpha = 0.2f),
    onSecondaryContainer = OceanSecondary,
    tertiary = OceanTertiary,
    onTertiary = OceanOnTertiary,
    background = OceanBackground,
    onBackground = OceanOnBackground,
    surface = OceanSurface,
    onSurface = OceanOnSurface,
    surfaceVariant = OceanSurfaceVariant,
    onSurfaceVariant = OceanOnSurfaceVariant,
    error = ErrorRed
)

private val FireColorScheme = darkColorScheme(
    primary = FirePrimary,
    onPrimary = FireOnPrimary,
    primaryContainer = FirePrimary.copy(alpha = 0.25f),
    onPrimaryContainer = FirePrimary,
    secondary = FireSecondary,
    onSecondary = FireOnSecondary,
    secondaryContainer = FireSecondary.copy(alpha = 0.2f),
    onSecondaryContainer = FireSecondary,
    tertiary = FireTertiary,
    onTertiary = FireOnTertiary,
    background = FireBackground,
    onBackground = FireOnBackground,
    surface = FireSurface,
    onSurface = FireOnSurface,
    surfaceVariant = FireSurfaceVariant,
    onSurfaceVariant = FireOnSurfaceVariant,
    error = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimary.copy(alpha = 0.25f),
    onPrimaryContainer = DarkPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondary.copy(alpha = 0.2f),
    onSecondaryContainer = DarkSecondary,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = ErrorRed
)

@Composable
fun KasuwanciTheme(
    selectedTheme: String = "neon",
    content: @Composable () -> Unit
) {
    val colorScheme = when (selectedTheme.lowercase()) {
        "ocean" -> OceanColorScheme
        "fire" -> FireColorScheme
        "dark" -> DarkColorScheme
        else -> NeonColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
