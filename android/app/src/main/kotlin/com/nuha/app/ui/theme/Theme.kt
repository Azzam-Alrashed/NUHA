package com.nuha.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/** App theme. The app is Arabic-only, so layout is always right-to-left regardless of device locale. */
@Composable
fun NuhaTheme(appearance: String = "colourful", reducedMotion: Boolean = true, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl,
        com.nuha.app.ui.identity.LocalIdentity provides com.nuha.app.ui.identity.paletteFor(appearance),
        com.nuha.app.ui.identity.LocalReducedMotion provides reducedMotion) {
        MaterialTheme(
            colorScheme = NuhaColors.copy(
                primary = com.nuha.app.ui.identity.paletteFor(appearance).ink,
                background = com.nuha.app.ui.identity.paletteFor(appearance).paper,
                onBackground = com.nuha.app.ui.identity.paletteFor(appearance).ink,
                onSurface = com.nuha.app.ui.identity.paletteFor(appearance).ink,
                outline = com.nuha.app.ui.identity.paletteFor(appearance).ink,
            ),
            typography = NuhaTypography,
            shapes = NuhaShapes,
            content = content,
        )
    }
}
