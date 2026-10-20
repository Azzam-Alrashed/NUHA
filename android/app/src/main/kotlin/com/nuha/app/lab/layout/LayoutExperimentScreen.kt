package com.nuha.app.lab.layout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.nuha.app.ui.components.NuhaMinTarget
import com.nuha.app.ui.components.NuhaSecondaryButton
import com.nuha.app.ui.components.TaskScaffold

private const val COUNT = 10
private const val COLUMNS = 5
private const val ROWS = 2
private val GAP = 16.dp

/**
 * Experiment 3: a complete activity (ordered number tiles, like the flower garden) that must fit
 * the window with no scrolling. Tile size is derived from the space actually available, so the
 * requested number can never be off-screen — the legacy app's problem.
 */
@Composable
fun LayoutExperimentScreen(onBack: () -> Unit) {
    var next by remember { mutableIntStateOf(1) }
    var wrongTaps by remember { mutableIntStateOf(0) }
    // Fixed shuffled order so the task is not just reading left to right.
    val order = remember { listOf(7, 3, 10, 1, 5, 9, 2, 6, 4, 8) }

    TaskScaffold(
        title = "تجربة 3: بستان الأرقام",
        instruction = if (next <= COUNT) "المس الرقم $next" else "أحسنت! نمت كل الأزهار 🌸",
        onBack = onBack,
        actions = {
            NuhaSecondaryButton("ابدأ من جديد", onClick = { next = 1; wrongTaps = 0 })
        },
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Diagnostics sit outside the measured area, so tile size never depends on how much the text grows.
            var area by remember { mutableStateOf(DpSize.Zero) }
            var tileDp by remember { mutableStateOf(0.dp) }
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val tile = tileSize(maxWidth, maxHeight)
                SideEffect { area = DpSize(maxWidth, maxHeight); tileDp = tile }
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(GAP, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    order.chunked(COLUMNS).forEach { rowNumbers ->
                        Row(horizontalArrangement = Arrangement.spacedBy(GAP)) {
                            rowNumbers.forEach { n ->
                                NumberTile(
                                    number = n,
                                    done = n < next,
                                    size = tile,
                                    onClick = { if (n == next) next++ else if (n >= next) wrongTaps++ },
                                )
                            }
                        }
                    }
                }
            }
            Diagnostics(tileDp, area.width, area.height, wrongTaps)
        }
    }
}

/** Largest square tile that fits COLUMNS × ROWS in the space. */
private fun tileSize(width: Dp, height: Dp): Dp {
    val byWidth = (width - GAP * (COLUMNS - 1)) / COLUMNS
    val byHeight = (height - GAP * (ROWS - 1)) / ROWS
    return min(byWidth, byHeight).coerceAtMost(220.dp)
}

@Composable
private fun NumberTile(number: Int, done: Boolean, size: Dp, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (done) scheme.primaryContainer else scheme.surface,
        border = BorderStroke(2.dp, scheme.outline),
        modifier = Modifier
            .size(size)
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = "الرقم $number"
                if (done) { stateDescription = "تم"; disabled() } else onClick { onClick(); true }
            }
            .clickable(enabled = !done, onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                if (done) "🌸" else "$number",
                // Scales with the tile; still respects the user's font scale through sp.
                fontSize = (size.value * 0.42f).sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Diagnostics(tile: Dp, w: Dp, h: Dp, wrongTaps: Int) {
    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val ok = tile >= NuhaMinTarget
    Text(
        "النافذة ${config.screenWidthDp}×${config.screenHeightDp}dp · مساحة النشاط ${w.value.toInt()}×${h.value.toInt()}dp · " +
            "حجم الخط ×${"%.2f".format(density.fontScale)} · المربع ${tile.value.toInt()}dp ${if (ok) "✓" else "⚠ أصغر من الحد الأدنى"} · " +
            "لمسات خاطئة: $wrongTaps",
        style = MaterialTheme.typography.bodySmall,
        color = if (ok) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
    )
}
