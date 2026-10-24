package com.nuha.app.activities

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuha.app.DraftArabic
import com.nuha.app.LocalNumerals
import com.nuha.app.Numerals
import com.nuha.app.Pending
import com.nuha.app.child.Activity
import com.nuha.app.child.activityHue
import com.nuha.app.task.ActivityPlan
import com.nuha.app.task.AnswerTile
import com.nuha.app.task.TaskScope
import com.nuha.app.task.TrialSpec
import com.nuha.app.ui.identity.IdentityPalette
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.scene.placeAt
import com.nuha.app.ui.theme.buttonWeight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * «زرع بستان الأزهار» (FR-GDN-001/002). Numbered seeds sit in fixed places; the child taps them in order and each correct
 * tap grows a flower. All seeds are on screen, there is no speed requirement, and timing exists only in a separately
 * chosen mode ([Pending.GardenTimed], off). Each trial records the order of taps, skipped numbers and help.
 * Numerals follow PLACEHOLDER OI-02. Draft content (docs/activities/garden.md).
 */
internal class GardenItem(val count: Int, val spots: List<Offset>)

/** Fixed places spread over the bed, far enough apart for 150 dp seeds on the 1280×800 dp tablet. */
private fun spotsFor(count: Int) = when (count) {
    3 -> listOf(Offset(.8f, .3f), Offset(.5f, .64f), Offset(.2f, .3f))
    4 -> listOf(Offset(.82f, .28f), Offset(.6f, .66f), Offset(.4f, .28f), Offset(.18f, .66f))
    else -> listOf(Offset(.86f, .28f), Offset(.68f, .66f), Offset(.5f, .28f), Offset(.32f, .66f), Offset(.14f, .28f))
}

/** Levels change one factor at a time (FR-ACT-012): three, four, then five seeds. */
internal class GardenPlan(completedSessions: Int, private val numerals: Numerals = Pending.DefaultNumerals) : ActivityPlan {
    private val level = Pending.levelFor(completedSessions, top = 2)
    private val count = level + 3
    override val activity = Activity.Garden
    override val taskVersion = 1
    override val difficulty = "level=$level;seeds=$count;timed=${Pending.GardenTimed}"
    @DraftArabic
    override fun trials(seed: Long): List<TrialSpec> {
        val r = Random(seed)
        return List(slots) { i ->
            TrialSpec(
                "garden-$i", "ازرع الأزهار بالترتيب: ابدأ بالرقم ${numerals.format(1)}.", "أحسنت! نمت كل الأزهار بالترتيب.",
                "نبدأ بالرقم الأصغر، ثم الذي بعده.", GardenItem(count, spotsFor(count).shuffled(r)),
            )
        }
    }
}

@Composable
internal fun GardenContent(scope: TaskScope) {
    val c = LocalIdentity.current
    val numerals = LocalNumerals.current
    val hue = c.activityHue(Activity.Garden)
    val item = scope.spec.item as GardenItem
    var next by remember(scope.trialNumber) { mutableIntStateOf(1) }
    val taps = remember(scope.trialNumber) { mutableStateListOf<String>() }
    var skips by remember(scope.trialNumber) { mutableIntStateOf(0) }
    BoxWithConstraints(Modifier.fillMaxSize().background(c.pastelGreen.tint, RoundedCornerShape(40.dp)), contentAlignment = AbsoluteAlignment.TopLeft) {
        val w = maxWidth; val h = maxHeight
        // A soil mound under each seed.
        Canvas(Modifier.fillMaxSize()) {
            for (spot in item.spots) {
                val at = Offset(spot.x * size.width, spot.y * size.height + 66.dp.toPx())
                drawOval(c.pastelGreen.lip.copy(alpha = .35f), at - Offset(120.dp.toPx(), 26.dp.toPx()), Size(240.dp.toPx(), 52.dp.toPx()))
            }
        }
        for (n in 1..item.count) {
            val spot = item.spots[n - 1]
            val grown = n < next || scope.solved
            AnswerTile(
                "seed-$n", n == next, scope, hue, numerals.format(n),
                Modifier.placeAt(w * spot.x, h * spot.y).size(150.dp), done = grown,
                onAnswer = { input, at ->
                    when {
                        grown -> Unit
                        n == next && n == item.count -> { taps += "$n"; scope.respond("seed-$n", true, input, at, detail = "taps=${taps.joinToString(",")};skips=$skips") }
                        n == next -> { taps += "$n"; scope.progress("seed-$n", input); next++ }
                        else -> { taps += "${n}x"; if (n > next) skips++; scope.respond("seed-$n", false, input, at) }
                    }
                },
            ) {
                if (grown) Bloom(c, Modifier.size(110.dp))
                else Text(numerals.format(n), color = c.ink, style = MaterialTheme.typography.displaySmall.buttonWeight().copy(fontSize = 64.sp, lineHeight = 72.sp))
            }
        }
    }
}

@Composable
private fun Bloom(c: IdentityPalette, modifier: Modifier) {
    Canvas(modifier.clearAndSetSemantics {}) {
        val s = size.minDimension / 100f
        drawContext.canvas.save(); drawContext.canvas.scale(s, s)
        drawLine(c.pastelGreen.lip, Offset(50f, 56f), Offset(50f, 96f), 6f)
        for (i in 0 until 6) { val a = i * PI / 3; drawCircle(c.monaLisa.fill, 15f, Offset(50f + (19 * cos(a)).toFloat(), 40f + (19 * sin(a)).toFloat())) }
        drawCircle(c.texasRose.fill, 11f, Offset(50f, 40f))
        drawContext.canvas.restore()
    }
}
