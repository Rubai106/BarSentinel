package com.barsentinel.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * BarSentinel ships dark-first: the approved design direction is the deep-navy
 * scheme, and the safety flows are used at night. A light scheme is a
 * Phase 7 polish item (spec §11) and lands as a second scheme here —
 * nothing in the screens reads raw colors, only theme tokens.
 */
private val DarkScheme = darkColorScheme(
    primary = Gold,
    onPrimary = GoldDarkText,
    primaryContainer = GoldDeep,
    onPrimaryContainer = GoldDarkText,
    secondary = InfoBlue,
    background = Navy800,
    onBackground = TextPrimary,
    surface = Navy700,
    onSurface = TextPrimary,
    surfaceVariant = Navy600,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    error = Danger,
    onError = TextPrimary
)

@Composable
fun BarSentinelTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = BarSentinelTypography,
        shapes = BarSentinelShapes,
        content = content
    )
}
