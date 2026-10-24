package com.nuha.app.ui.theme

import android.content.Context
import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.nuha.app.LocalNumerals
import com.nuha.app.Numerals
import com.nuha.app.Pending
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.LocalMotion
import com.nuha.app.ui.identity.LocalReducedMotion
import com.nuha.app.ui.identity.Motion
import com.nuha.app.ui.identity.paletteFor

/** App theme. The app is Arabic-only, so layout is always right-to-left regardless of device locale. */
@Composable
fun NuhaTheme(appearance: String = "colourful", motion: Motion = Motion.Reduced, numerals: Numerals = Pending.DefaultNumerals, content: @Composable () -> Unit) {
    val palette = paletteFor(appearance)
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalIdentity provides palette,
        LocalMotion provides motion,
        LocalReducedMotion provides (motion != Motion.Full),
        LocalNumerals provides numerals,
    ) {
        MaterialTheme(
            colorScheme = NuhaColors.copy(primary = palette.ink, background = palette.paper, onBackground = palette.ink, onSurface = palette.ink, outline = palette.ink),
            typography = NuhaTypography,
            shapes = NuhaShapes,
            content = content,
        )
    }
}

/** True when the device's animations are switched off (Settings, Accessibility, Remove animations). */
fun systemAnimationsOff(context: Context): Boolean =
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
