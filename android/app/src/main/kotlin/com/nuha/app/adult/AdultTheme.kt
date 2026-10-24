package com.nuha.app.adult

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.paletteFor
import com.nuha.app.ui.theme.AdultFamily

/**
 * The adult area's own Material 3 theme (FR-ADM-001): plain surfaces, the Readex Pro face, standard components, and
 * the identity's action hue as its one accent, so it reads as a different place from child mode. Always the colourful
 * palette and right-to-left, whatever the child's settings.
 */
@Composable
fun AdultTheme(content: @Composable () -> Unit) {
    val palette = paletteFor("colourful")
    val scheme = lightColorScheme(
        primary = palette.action.lip,
        onPrimary = Color.White,
        primaryContainer = palette.action.tint,
        onPrimaryContainer = palette.ink,
        secondaryContainer = palette.action.tint,
        onSecondaryContainer = palette.ink,
        background = Color(0xFFF7F6F9),
        onBackground = palette.ink,
        surface = Color.White,
        onSurface = palette.ink,
        surfaceContainerLow = Color.White,
        surfaceContainer = Color(0xFFF1EFF5),
        onSurfaceVariant = palette.ink.copy(alpha = .78f),
        outline = palette.ink.copy(alpha = .45f),
        outlineVariant = palette.ink.copy(alpha = .16f),
    )
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl, LocalIdentity provides palette) {
        MaterialTheme(colorScheme = scheme, typography = AdultTypography, content = content)
    }
}

private fun style(size: Int, line: Int, weight: Int = 400) =
    TextStyle(fontFamily = AdultFamily, fontSize = size.sp, lineHeight = line.sp, fontWeight = androidx.compose.ui.text.font.FontWeight(weight), letterSpacing = 0.sp)

private val AdultTypography = Typography(
    displaySmall = style(36, 48, 600),
    headlineMedium = style(28, 40, 600),
    headlineSmall = style(24, 34, 600),
    titleLarge = style(22, 32, 600),
    titleMedium = style(18, 28, 600),
    titleSmall = style(16, 24, 600),
    bodyLarge = style(18, 30),
    bodyMedium = style(16, 26),
    bodySmall = style(14, 22),
    labelLarge = style(16, 24, 600),
    labelMedium = style(14, 20, 600),
    labelSmall = style(13, 18, 600),
)
