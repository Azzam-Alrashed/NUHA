package com.nuha.app.child

import androidx.compose.runtime.staticCompositionLocalOf
import com.nuha.app.data.ChildSettings

/** The playing child's settings (FR-SET-002), for what the theme does not already carry: celebration, mascot, timing, size, map lock. */
val LocalChildSettings = staticCompositionLocalOf { ChildSettings(childId = 0) }
