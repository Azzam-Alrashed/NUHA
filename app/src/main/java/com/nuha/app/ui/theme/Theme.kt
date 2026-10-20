package com.nuha.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

// Calm, low-saturation palette. A starting point to test with children, not a final brand.
private val NuhaColors = lightColorScheme(
    primary = Color(0xFF2F6F73),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFE6E4),
    onPrimaryContainer = Color(0xFF0E3B3E),
    secondary = Color(0xFF8A6A3D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1E3CC),
    onSecondaryContainer = Color(0xFF3A2A12),
    tertiary = Color(0xFF5B6A9A),
    tertiaryContainer = Color(0xFFDDE2F5),
    background = Color(0xFFFAF7F2),
    onBackground = Color(0xFF1F2523),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F2523),
    surfaceVariant = Color(0xFFECE7DF),
    onSurfaceVariant = Color(0xFF4A504D),
    outline = Color(0xFF8C918E),
    error = Color(0xFFB4473A),
    errorContainer = Color(0xFFF6DAD5),
)

// Larger-than-default sizes for young readers and arm's-length table viewing.
private val NuhaTypography = Typography(
    displaySmall = TextStyle(fontSize = 40.sp, lineHeight = 52.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 32.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 26.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 24.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 20.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 20.sp, lineHeight = 32.sp),
    bodyMedium = TextStyle(fontSize = 18.sp, lineHeight = 28.sp),
    bodySmall = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    labelLarge = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
)

private val NuhaShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
)

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
