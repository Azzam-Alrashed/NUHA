package com.nuha.app.task

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.nuha.app.DraftArabic
import com.nuha.app.Pending
import com.nuha.app.child.Activity
import com.nuha.app.child.ChildCopy
import com.nuha.app.child.RoundIconButton
import com.nuha.app.child.activityHue
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.NuhaAvatar
import com.nuha.app.ui.identity.starPath
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

// MILESTONE 2 SCAFFOLDING: stand-in tasks so the framework runs on every stop of the map. Milestone 3 replaces them
// activity by activity with the real tasks; nothing here is learning content, and its Arabic is draft.

/** Drawing activities get the trace stand-in; the rest get the tap stand-in. */
private val DrawingActivities = setOf(Activity.SocialPath, Activity.Star, Activity.Maze, Activity.Letters)

fun demoPlanFor(activity: Activity): ActivityPlan = if (activity in DrawingActivities) TraceDemoPlan(activity) else TapDemoPlan(activity)

@Composable
fun DemoContent(activity: Activity, scope: TaskScope) {
    if (activity in DrawingActivities) TraceDemo(activity, scope) else TapDemo(activity, scope)
}

@DraftArabic
private val ShapeNames = mapOf("triangle" to "المثلث", "sun" to "الشمس", "squircle" to "المربّع", "dome" to "القبّة")

private class TapItem(val target: String, val options: List<String>)

private class TapDemoPlan(override val activity: Activity) : ActivityPlan {
    override val taskVersion = 0
    override val difficulty = "demo=tap;options=3"
    override fun trials(seed: Long): List<TrialSpec> {
        val r = Random(seed)
        return List(slots) { i ->
            val target = ShapeNames.keys.random(r)
            val options = (listOf(target) + (ShapeNames.keys - target).shuffled(r).take(2)).shuffled(r)
            val name = ShapeNames.getValue(target)
            TrialSpec("tap-$i-$target", "اضغط على $name", "أحسنت! هذا $name.", "هذا $name. اضغط عليه.", TapItem(target, options))
        }
    }
}

/** Three shape buddies in fixed places; one is named. */
@Composable
private fun TapDemo(activity: Activity, scope: TaskScope) {
    val hue = LocalIdentity.current.activityHue(activity)
    val item = scope.spec.item as TapItem
    Row(horizontalArrangement = Arrangement.spacedBy(56.dp), verticalAlignment = Alignment.CenterVertically) {
        for (kind in item.options) AnswerTile(kind, kind == item.target, scope, hue, ShapeNames.getValue(kind), Modifier.size(230.dp)) {
            NuhaAvatar(kind, Modifier.fillMaxSize(.86f))
        }
    }
}

private class TraceDemoPlan(override val activity: Activity) : ActivityPlan {
    override val taskVersion = 0
    override val difficulty = "demo=trace;tolerance=${Pending.TraceTolerance}"
    override fun trials(seed: Long): List<TrialSpec> {
        val r = Random(seed)
        return List(slots) { i ->
            val shape = Guides.keys.random(r)
            TrialSpec(
                "trace-$i-$shape", "ارسم فوق الخط من النقطة إلى النجمة", "أحسنت! رسمت الخط كله.",
                "انظر، نبدأ من النقطة ونمشي فوق الخط حتى النجمة.", Guides.getValue(shape),
            )
        }
    }
}

/** Guide lines in 0..1 card coordinates. Each starts on the right, where writing starts in RTL, and ends on the left. */
private val Guides: Map<String, List<StrokePoint>> = mapOf(
    "straight" to sample { t -> .86f - .72f * t to .5f },
    "wave" to sample { t -> .86f - .72f * t to .5f + .18f * sin(t * 2 * PI).toFloat() },
    "arch" to sample { t -> .86f - .72f * t to .72f - .5f * sin(t * PI).toFloat() },
    "slope" to sample { t -> .86f - .72f * t to .25f + .5f * t },
)

private fun sample(f: (Float) -> Pair<Float, Float>) = List(31) { i -> f(i / 30f).let { (x, y) -> StrokePoint(x, y, 0) } }

/**
 * Draw over a guide from the dot to the star. A drawing that covers the guide within tolerance solves the trial; a
 * drawing that reaches the star, or a third try, without covering it counts as a try and clears gently. Hint level
 * draws the guide bolder; model level runs a dot along it (reduced motion: arrows instead).
 */
@Composable
private fun TraceDemo(activity: Activity, scope: TaskScope) {
    val c = LocalIdentity.current
    val hue = c.activityHue(activity)
    val guide = scope.spec.item as List<*>
    @Suppress("UNCHECKED_CAST") val points = guide as List<StrokePoint>
    val strokes = remember(scope.trialNumber) { mutableStateListOf<SavedStroke>() }
    var area by remember { mutableStateOf(IntSize.Zero) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    LaunchedEffect(scope.wrongTick) { if (scope.wrongTick > 0) { kotlinx.coroutines.delay(450); strokes.clear() } }
    val runner = remember(scope.trialNumber) { Animatable(0f) }
    LaunchedEffect(scope.level >= Ladder.Model, scope.trialNumber) {
        if (scope.level >= Ladder.Model && !scope.reducedMotion) runner.animateTo(1f, infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart))
    }
    Column(Modifier.fillMaxSize().padding(bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(40.dp))
                .onGloballyPositioned { area = it.size; origin = it.positionInRoot() }
                .semantics { contentDescription = scope.spec.instruction },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val pts = points.map { Offset(it.x * size.width, it.y * size.height) }
                val path = Path().apply { moveTo(pts[0].x, pts[0].y); pts.drop(1).forEach { lineTo(it.x, it.y) } }
                val bold = scope.level >= Ladder.Hint
                drawPath(path, hue.tint, style = Stroke(46.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawPath(
                    path, if (bold) hue.lip else hue.fill,
                    style = Stroke((if (bold) 8.dp else 5.dp).toPx(), cap = StrokeCap.Round, pathEffect = if (bold) null else PathEffect.dashPathEffect(floatArrayOf(18.dp.toPx(), 14.dp.toPx()))),
                )
                drawCircle(hue.fill, 22.dp.toPx(), pts.first()); drawCircle(hue.lip, 22.dp.toPx(), pts.first(), style = Stroke(3.dp.toPx()))
                val star = starPath(pts.last(), 30.dp.toPx())
                drawPath(star, c.texasRose.fill); drawPath(star, c.texasRose.lip, style = Stroke(3.dp.toPx(), join = StrokeJoin.Round))
                if (scope.level >= Ladder.Model) {
                    if (!scope.reducedMotion) drawCircle(c.action.fill, 16.dp.toPx(), pts[(runner.value * (pts.size - 1)).toInt()])
                    else for (i in listOf(8, 15, 22)) {
                        val a = pts[i]; val b = pts[i + 2]; val d = (b - a) / hypot(b.x - a.x, b.y - a.y); val n = Offset(-d.y, d.x)
                        val tip = a + d * 18.dp.toPx()
                        drawPath(Path().apply { moveTo(tip.x, tip.y); val back = tip - d * 18.dp.toPx(); lineTo(back.x + n.x * 12.dp.toPx(), back.y + n.y * 12.dp.toPx()); lineTo(back.x - n.x * 12.dp.toPx(), back.y - n.y * 12.dp.toPx()); close() }, c.action.fill)
                    }
                }
            }
            InkSurface(strokes, c.ink, widthDp = 9f, enabled = !scope.solved, onStroke = { s ->
                strokes += s
                if (area.width == 0) return@InkSurface
                val score = traceScore(points, strokes, aspect = area.width / area.height.toFloat())
                val end = s.points.lastOrNull()
                val at = end?.let { origin + Offset(it.x * area.width, it.y * area.height) }
                if (score.accepted) scope.respond("trace", true, s.tool, at, StrokeCodec.encode(strokes))
                else {
                    val last = points.last()
                    val reachedStar = end != null && hypot((end.x - last.x) * area.width, (end.y - last.y) * area.height) < .1f * minOf(area.width, area.height)
                    if (reachedStar || strokes.size >= 3) scope.respond("trace", false, s.tool, null)
                }
            }, modifier = Modifier.fillMaxSize())
            Row(Modifier.align(AbsoluteAlignment.TopLeft).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RoundIconButton("undo", ChildCopy.Undo, c.action, null) { if (strokes.isNotEmpty() && !scope.solved) strokes.removeAt(strokes.lastIndex) }
                RoundIconButton("clear", ChildCopy.Clear, c.action, null) { if (!scope.solved) strokes.clear() }
            }
        }
    }
}
