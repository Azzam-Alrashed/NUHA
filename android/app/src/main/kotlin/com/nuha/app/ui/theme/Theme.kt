package com.nuha.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/** App theme. The app is Arabic-only, so layout is always right-to-left regardless of device locale. */
@Composable
fun NuhaTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = NuhaColors,
            typography = NuhaTypography,
            shapes = NuhaShapes,
            content = content,
        )
    }
}
