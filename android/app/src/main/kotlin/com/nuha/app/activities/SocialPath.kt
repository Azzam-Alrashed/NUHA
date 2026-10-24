package com.nuha.app.activities

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import com.nuha.app.task.ActivityPlan
import com.nuha.app.task.InkSurface
import com.nuha.app.task.Ladder
import com.nuha.app.task.SavedStroke
import com.nuha.app.task.StrokeCodec
import com.nuha.app.task.TaskScope
import com.nuha.app.task.TrialSpec
import com.nuha.app.ui.identity.IdentityPalette
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.NuhaAvatar
import com.nuha.app.ui.scene.placeAt
import kotlin.random.Random
import kotlinx.coroutines.launch

/**
 * «تتبع مسار الأشكال الاجتماعية» (FR-PTH-001/002): draw one line from a buddy to its friend, around the rocks. The first
 * trial of a session shows an example route; levels change route complexity (rock count) and corridor width (rock size)
 * one at a time. The record keeps the route, rock contacts (as misses) and restarts (attempts). This is route planning
 * and motor practice, not a measure of social skills. Draft content (docs/activities/path.md).
 */
internal class RouteItem(val rocks: List<Circle>, val safe: List<Offset>, val showExample: Boolean, val me: String, val friend: String)

private val Start = Offset(.88f, .5f)
private val End = Offset(.12f, .5f)

internal class PathPlan(completedSessions: Int) : ActivityPlan {
    private val level = Pending.levelFor(completedSessions, top = 2)
    private val rocks = if (level == 0) 1 else 2
    private val radius = if (level < 2) .16f else .22f
    override val activity = Activity.SocialPath
    override val taskVersion = 1
    override val difficulty = "level=$level;rocks=$rocks;rockRadius=$radius"
    @DraftArabic
    override fun trials(seed: Long): List<TrialSpec> {
        val r = Random(seed)
        val pairs = listOf("squircle" to "clover", "dome" to "sun", "bean" to "triangle").shuffled(r)
        return List(slots) { i ->
            val above = r.nextBoolean()
            val (obstacles, safe) = if (rocks == 1) {
                listOf(Circle(.5f, .5f, radius)) to listOf(Start, Offset(.5f, if (above) .16f else .84f), End)
            } else {
                listOf(Circle(.63f, .5f, radius), Circle(.37f, .5f, radius)) to
                    listOf(Start, Offset(.63f, if (above) .16f else .84f), Offset(.5f, .5f), Offset(.37f, if (above) .84f else .16f), End)
            }
            val (me, friend) = pairs[i % pairs.size]
            TrialSpec(
                "route-$i-${if (above) "up" else "down"}", "ارسم طريقًا من صديقنا إلى صديقه، وابتعد عن الصخور.",
                "أحسنت! وصل صديقنا إلى صديقه بأمان.", "انظر، نمرّ بجانب الصخرة ولا نلمسها.",
                RouteItem(obstacles, safe, showExample = i == 0, me, friend),
            )
        }
    }
}

@Composable
internal fun SocialPathContent(scope: TaskScope) {
    val c = LocalIdentity.current
    val hue = c.activityHue(Activity.SocialPath)
    val item = scope.spec.item as RouteItem
    val strokes = remember(scope.trialNumber) { mutableStateListOf<SavedStroke>() }
    var size by remember { mutableStateOf(IntSize.Zero) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var restarts by remember(scope.trialNumber) { mutableIntStateOf(0) }
    var contacts by remember(scope.trialNumber) { mutableIntStateOf(0) }
    // Rocks the last route touched wobble, so the child sees where the line went wrong.
    var bumped by remember(scope.trialNumber) { mutableStateOf(emptySet<Int>()) }
    val wobble = remember(scope.trialNumber) { Animatable(0f) }
    val launch = rememberCoroutineScope()
    LaunchedEffect(scope.wrongTick) { if (scope.wrongTick > 0) { kotlinx.coroutines.delay(500); strokes.clear(); bumped = emptySet() } }
    val runner = remember(scope.trialNumber) { Animatable(0f) }
    LaunchedEffect(scope.level >= Ladder.Model, scope.trialNumber) {
        if (scope.level >= Ladder.Model && !scope.reducedMotion) runner.animateTo(1f, infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart))
    }
    val showSafe = (item.showExample && strokes.isEmpty() && restarts == 0) || scope.level >= Ladder.Hint
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Color.White, RoundedCornerShape(40.dp))
            .onGloballyPositioned { size = it.size; origin = it.positionInRoot() }
            .semantics { contentDescription = scope.spec.instruction },
        contentAlignment = AbsoluteAlignment.TopLeft,
    ) {
        val w = maxWidth; val h = maxHeight
        Canvas(Modifier.fillMaxSize()) {
            fun px(o: Offset) = Offset(o.x * size.width, o.y * size.height)
            val unit = minOf(size.width, size.height)
            item.rocks.forEachIndexed { i, rock ->
                val shake = if (i in bumped) wobble.value * 10.dp.toPx() else 0f
                rock(c, px(Offset(rock.x, rock.y)) + Offset(shake, 0f), rock.r * unit)
            }
            if (showSafe) {
                val pts = com.nuha.app.task.smoothGuide(item.safe).map { px(Offset(it.x, it.y)) }
                drawPath(Path().apply { moveTo(pts[0].x, pts[0].y); pts.drop(1).forEach { lineTo(it.x, it.y) } }, hue.lip.copy(alpha = .7f), style = Stroke(6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(16.dp.toPx(), 14.dp.toPx()))))
                if (scope.level >= Ladder.Model && !scope.reducedMotion) drawCircle(c.action.fill, 14.dp.toPx(), pts[(runner.value * (pts.size - 1)).toInt()])
            }
        }
        NuhaAvatar(item.me, Modifier.placeAt(w * Start.x, h * Start.y).size(130.dp), gaze = Offset(-1f, 0f))
        NuhaAvatar(item.friend, Modifier.placeAt(w * End.x, h * End.y).size(130.dp), gaze = Offset(1f, 0f))
        InkSurface(strokes, c.ink, widthDp = 9f, enabled = !scope.solved, onStroke = { s ->
            strokes += s
            scope.active()
            if (size == IntSize.Zero) return@InkSurface
            val check = checkRoute(s.points, Start.x, Start.y, End.x, End.y, item.rocks, size.width / size.height.toFloat())
            val end = s.points.lastOrNull()?.let { origin + Offset(it.x * size.width, it.y * size.height) }
            contacts += check.touched.size
            repeat(check.touched.size) { scope.miss() }
            bumped = check.touched
            if (bumped.isNotEmpty() && !scope.reducedMotion) launch.launch {
                wobble.animateTo(0f, keyframes { durationMillis = 420; 1f at 60; -1f at 160; .6f at 260; -.3f at 340 })
            }
            if (check.clear) scope.respond("route", true, s.tool, end, StrokeCodec.encode(strokes), "contacts=$contacts;restarts=$restarts")
            else { restarts++; scope.respond("route", false, s.tool, null, StrokeCodec.encode(strokes)) }
        }, modifier = Modifier.fillMaxSize())
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RoundIconButton("clear", ChildCopy.Clear, c.action, null) { if (!scope.solved) strokes.clear() }
        }
    }
}

/** A flat boulder covering about the circle the route check uses. Drawn from ink tints, so it follows comfort modes. */
private fun DrawScope.rock(c: IdentityPalette, center: Offset, r: Float) {
    val body = c.ink.copy(alpha = .2f).compositeOver(Color.White)
    val lip = c.ink.copy(alpha = .38f).compositeOver(Color.White)
    val lumps = listOf(Offset(0f, .06f) to .9f, Offset(-.44f, .3f) to .62f, Offset(.46f, .28f) to .6f)
    for ((o, k) in lumps) drawCircle(lip, r * k, center + Offset(o.x * r, (o.y + .1f) * r))
    for ((o, k) in lumps) drawCircle(body, r * k, center + Offset(o.x * r, o.y * r))
    drawCircle(Color.White.copy(alpha = .5f), r * .15f, center + Offset(-.32f * r, -.38f * r))
}
