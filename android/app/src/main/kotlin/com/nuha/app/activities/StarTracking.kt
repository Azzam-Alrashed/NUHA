package com.nuha.app.activities

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.nuha.app.DraftArabic
import com.nuha.app.Pending
import com.nuha.app.child.Activity
import com.nuha.app.child.activityHue
import com.nuha.app.task.ActivityPlan
import com.nuha.app.task.InputType
import com.nuha.app.task.Ladder
import com.nuha.app.task.SavedStroke
import com.nuha.app.task.StrokeCodec
import com.nuha.app.task.StrokePoint
import com.nuha.app.task.TaskScope
import com.nuha.app.task.TrialSpec
import com.nuha.app.task.toInput
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.starPath
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * «تتبع النجوم الهادئة» (FR-STR-001/002). The star waits at the start of its path until the child touches it, then moves
 * only while the finger or pen stays down; lifting pauses it, so there is no time pressure and resting is allowed.
 * A run counts when enough of the contact time was inside the ring ([Pending.StarInTargetMin]). The record keeps time
 * in target, contact time, lifts, ring size and speed, never an attention score. Draft content (docs/activities/star.md).
 */
internal class StarRun(val path: String, val points: List<Offset>, val radiusDp: Float, val durationMs: Long)

/** Paths in 0..1 card coordinates, starting on the right as reading does. */
internal val StarPaths: Map<String, List<Offset>> = mapOf(
    "gentle" to List(61) { i -> val t = i / 60f; Offset(.86f - .72f * t, .5f + .14f * sin(t * 2 * PI).toFloat()) },
    "arch" to List(61) { i -> val t = i / 60f; Offset(.86f - .72f * t, .7f - .42f * sin(t * PI).toFloat()) },
    "loop" to List(81) { i -> val t = i / 80f; val a = t * 2 * PI; Offset(.5f + .34f * kotlin.math.cos(a).toFloat() * (1 - .2f * t), .5f + .3f * sin(a).toFloat()) },
)

/** Levels change one factor at a time (FR-ACT-012): ring size, then speed, then path shape. */
internal class StarPlan(completedSessions: Int) : ActivityPlan {
    private val level = Pending.levelFor(completedSessions, top = 3)
    private val radius = if (level == 0) 80f else 64f
    private val duration = if (level < 2) 9_000L else 7_000L
    private val paths = if (level < 3) listOf("gentle", "arch") else listOf("gentle", "arch", "loop")
    override val activity = Activity.Star
    override val taskVersion = 1
    override val difficulty = "level=$level;radiusDp=${radius.toInt()};durationMs=$duration;paths=${paths.joinToString("+")}"
    @DraftArabic
    override fun trials(seed: Long): List<TrialSpec> {
        val r = Random(seed)
        return List(slots) { i ->
            val path = paths.random(r)
            TrialSpec(
                key = "run-$i-$path",
                instruction = "ضع إصبعك على النجمة وامشِ معها داخل الدائرة.",
                praise = "أحسنت! مشيت مع النجمة حتى النهاية.",
                model = "انظر، نمشي مع النجمة ببطء ونبقى داخل الدائرة.",
                item = StarRun(path, StarPaths.getValue(path), radius, duration),
            )
        }
    }
}

/** Position along a polyline at fraction [t] of its length. */
internal fun along(points: List<Offset>, t: Float): Offset {
    val lengths = points.zipWithNext { a, b -> (b - a).getDistance() }
    val total = lengths.sum()
    var left = t.coerceIn(0f, 1f) * total
    for ((i, len) in lengths.withIndex()) {
        if (left <= len) return points[i] + (points[i + 1] - points[i]) * (if (len == 0f) 0f else left / len)
        left -= len
    }
    return points.last()
}

@Composable
internal fun StarTrackingContent(scope: TaskScope) {
    val c = LocalIdentity.current
    val hue = c.activityHue(Activity.Star)
    val run = scope.spec.item as StarRun
    val density = LocalDensity.current
    // Help widens the ring at the hint level; it is recorded as help, not as a difficulty change.
    val radiusPx = with(density) { run.radiusDp.dp.toPx() } * if (scope.level >= Ladder.Hint) 1.3f else 1f
    var attempt by remember(scope.trialNumber) { mutableIntStateOf(0) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var progress by remember(scope.trialNumber, attempt) { mutableFloatStateOf(0f) }
    var touching by remember { mutableStateOf(false) }
    var pointer by remember { mutableStateOf(Offset.Zero) }
    var input by remember { mutableStateOf(InputType.Other) }
    var inTarget by remember(scope.trialNumber, attempt) { mutableLongStateOf(0L) }
    var contact by remember(scope.trialNumber, attempt) { mutableLongStateOf(0L) }
    var lifts by remember(scope.trialNumber, attempt) { mutableIntStateOf(0) }
    val trail = remember(scope.trialNumber, attempt) { mutableStateListOf<StrokePoint>() }
    val strokes = remember(scope.trialNumber, attempt) { mutableListOf<SavedStroke>() }
    // The stroke under the finger right now; a run can end before the finger lifts.
    val current = remember(scope.trialNumber, attempt) { mutableListOf<StrokePoint>() }
    var startedAt by remember(scope.trialNumber, attempt) { mutableLongStateOf(0L) }
    fun px(o: Offset) = Offset(o.x * size.width, o.y * size.height)
    // Read inside the gesture loop without restarting it when help widens the ring or the card resizes.
    val ring by androidx.compose.runtime.rememberUpdatedState(radiusPx)
    val star = px(along(run.points, progress))

    fun finish() {
        val share = if (contact == 0L) 0f else inTarget / contact.toFloat()
        val detail = "inTargetMs=$inTarget;contactMs=$contact;lifts=$lifts;radiusDp=${run.radiusDp.toInt()};durationMs=${run.durationMs};share=${"%.2f".format(java.util.Locale.ROOT, share)}"
        val encoded = StrokeCodec.encode(strokes + listOfNotNull(current.takeIf { it.isNotEmpty() }?.let { SavedStroke(input, it.toList()) }))
        if (share >= Pending.StarInTargetMin) scope.respond("run", true, input, origin + star, encoded, detail)
        else { scope.respond("run", false, input, null, encoded); attempt++ }
    }

    LaunchedEffect(scope.trialNumber, attempt, size) {
        var last = withFrameMillis { it }
        while (true) {
            withFrameMillis { now ->
                val dt = now - last; last = now
                if (touching && !scope.solved && progress < 1f && size != IntSize.Zero) {
                    scope.active()
                    contact += dt
                    if ((pointer - px(along(run.points, progress))).getDistance() <= radiusPx) inTarget += dt
                    progress = (progress + dt / run.durationMs.toFloat()).coerceAtMost(1f)
                    if (progress >= 1f) { touching = false; finish() }
                }
            }
        }
    }

    // Model level: a ghost dot walks the path to show the way (arrows under reduced motion).
    val ghost = remember(scope.trialNumber) { Animatable(0f) }
    LaunchedEffect(scope.level >= Ladder.Model, scope.trialNumber) {
        if (scope.level >= Ladder.Model && !scope.reducedMotion) ghost.animateTo(1f, infiniteRepeatable(tween(run.durationMs.toInt() / 2, easing = LinearEasing), RepeatMode.Restart))
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.White, RoundedCornerShape(40.dp))
            .onGloballyPositioned { size = it.size; origin = it.positionInRoot() }
            .semantics { contentDescription = scope.spec.instruction }
            .pointerInput(scope.trialNumber, attempt) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    // A run starts, or continues after a lift, by touching the star's ring.
                    if (scope.solved || (down.position - px(along(run.points, progress))).getDistance() > ring * 1.4f) return@awaitEachGesture
                    if (startedAt == 0L) startedAt = down.uptimeMillis
                    input = down.type.toInput(); pointer = down.position; touching = true
                    current.clear()
                    fun note(p: Offset, ms: Long) { val sp = StrokePoint(p.x / size.width, p.y / size.height, ms - startedAt); current += sp; trail += sp; if (trail.size > 40) trail.removeAt(0) }
                    note(down.position, down.uptimeMillis)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        pointer = change.position; note(change.position, change.uptimeMillis); change.consume()
                    }
                    if (touching && progress < 1f) lifts++
                    touching = false
                    if (current.isNotEmpty()) { strokes += SavedStroke(input, current.toList()); current.clear() }
                }
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            if (size == IntSize.Zero) return@Canvas
            val pts = run.points.map(::px)
            val path = Path().apply { moveTo(pts[0].x, pts[0].y); pts.drop(1).forEach { lineTo(it.x, it.y) } }
            drawPath(path, hue.tint, style = Stroke(10.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(path, hue.lip.copy(alpha = .5f), style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 12.dp.toPx()))))
            if (scope.level >= Ladder.Model) {
                if (!scope.reducedMotion) drawCircle(c.action.fill.copy(alpha = .7f), 14.dp.toPx(), px(along(run.points, ghost.value)))
                else for (f in listOf(.25f, .5f, .75f)) {
                    val a = px(along(run.points, f)); val b = px(along(run.points, f + .02f)); val d = (b - a) / (b - a).getDistance().coerceAtLeast(1f); val n = Offset(-d.y, d.x)
                    val tip = a + d * 16.dp.toPx(); val back = tip - d * 20.dp.toPx()
                    drawPath(Path().apply { moveTo(tip.x, tip.y); lineTo(back.x + n.x * 12.dp.toPx(), back.y + n.y * 12.dp.toPx()); lineTo(back.x - n.x * 12.dp.toPx(), back.y - n.y * 12.dp.toPx()); close() }, c.action.fill)
                }
            }
            // The child's recent path, so they can see where they are.
            if (trail.size > 1) drawPath(Path().apply { val p0 = px(Offset(trail[0].x, trail[0].y)); moveTo(p0.x, p0.y); trail.drop(1).forEach { val p = px(Offset(it.x, it.y)); lineTo(p.x, p.y) } }, c.ink.copy(alpha = .35f), style = Stroke(6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            val inside = touching && (pointer - star).getDistance() <= radiusPx
            drawCircle(if (inside) hue.fill.copy(alpha = .45f) else hue.tint.copy(alpha = .7f), radiusPx, star)
            drawCircle(hue.lip, radiusPx, star, style = Stroke(5.dp.toPx()))
            val s = starPath(star, 30.dp.toPx())
            drawPath(s, hue.fill); drawPath(s, hue.lip, style = Stroke(3.dp.toPx(), join = StrokeJoin.Round))
        }
    }
}

