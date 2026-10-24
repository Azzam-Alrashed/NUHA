package com.nuha.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Larger-than-default sizes for young readers and arm's-length table viewing.
private val BaseTypography = Typography(
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

// Readex Pro is a variable font. Each weight gets its own wght axis value; without that, Android fakes
// heavier weights from the regular instance. The axis stops at 700, so ExtraBold and Black render at 700
// until a heavier face is bundled.
private const val READEX_MAX_WEIGHT = 700
private fun readex(weight: FontWeight) = Font(
    com.nuha.app.R.font.readex_pro,
    weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight.coerceAtMost(READEX_MAX_WEIGHT))),
)
private val ArabicFamily = FontFamily(
    listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold, FontWeight.Black).map(::readex),
)
private fun TextStyle.arabic() = copy(fontFamily = ArabicFamily, letterSpacing = 0.sp)
internal val NuhaTypography = Typography(
    displaySmall = BaseTypography.displaySmall.arabic().copy(fontSize = 44.sp, lineHeight = 66.sp),
    headlineMedium = BaseTypography.headlineMedium.arabic(),
    headlineSmall = BaseTypography.headlineSmall.arabic(),
    titleLarge = BaseTypography.titleLarge.arabic(),
    titleMedium = BaseTypography.titleMedium.arabic(),
    bodyLarge = BaseTypography.bodyLarge.arabic(),
    bodyMedium = BaseTypography.bodyMedium.arabic(),
    bodySmall = BaseTypography.bodySmall.arabic(),
    labelLarge = BaseTypography.labelLarge.arabic(),
    labelMedium = BaseTypography.labelMedium.arabic(),
    labelSmall = BaseTypography.labelSmall.arabic(),
)

// Playful Soft-Flat weights (docs/research/ui-style.md, Typography). Applied per screen rather than in the
// scale above, so the lab screens keep their weights. Line heights and zero letter-spacing are unchanged.
/** Screen titles: Black from 24sp; ExtraBold below that, where Black closes small Arabic counters. */
fun TextStyle.titleWeight() = copy(fontWeight = if (fontSize.isSp && fontSize.value >= 24f) FontWeight.Black else FontWeight.ExtraBold)
/** Buttons. */
fun TextStyle.buttonWeight() = copy(fontWeight = FontWeight.ExtraBold)
/** Secondary labels. */
fun TextStyle.labelWeight() = copy(fontWeight = FontWeight.SemiBold)
