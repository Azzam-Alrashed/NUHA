package com.nuha.app.task

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.nuha.app.child.ChildCopy
import com.nuha.app.child.RoundIconButton
import com.nuha.app.ui.identity.Hue
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.starPath
import kotlin.math.hypot

/**
 * A tracing pad shared by drawing tasks: a guide line from a start dot to an end star, an ink surface, and undo and
 * clear (FR-ACT-017). A drawing that covers the guide within tolerance solves the trial (FR-ACT-018); one that reaches
 * the end, or a third stroke, without covering it counts as a try, is kept as an attempt, and clears gently. Hint level
 * draws the guide bold; model level runs a dot along it (arrows under reduced motion). [guide] is in 0..1 coordinates
 * of the pad; [square] keeps the pad square (letters, shapes). [decorate] draws extras such as a letter's dots.
 */
@Composable
fun TracePad(
    guide: List<StrokePoint>,
    scope: TaskScope,
    hue: Hue,
    modifier: Modifier = Modifier,
    choice: String = "trace",
    showEnds: Boolean = true,
    decorate: DrawScope.(toPx: (Offset) -> Offset) -> Unit = {},
) {
    val c = LocalIdentity.current
    val strokes = remember(scope.trialNumber) { mutableStateListOf<SavedStroke>() }
    var area by remember { mutableStateOf(IntSize.Zero) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    LaunchedEffect(scope.wrongTick) { if (scope.wrongTick > 0) { kotlinx.coroutines.delay(450); strokes.clear() } }
    val runner = remember(scope.trialNumber) { Animatable(0f) }
    LaunchedEffect(scope.level >= Ladder.Model, scope.trialNumber) {
        if (scope.level >= Ladder.Model && !scope.reducedMotion) runner.animateTo(1f, infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier
                .background(Color.White, RoundedCornerShape(40.dp))
                .onGloballyPositioned { area = it.size; origin = it.positionInRoot() }
                .semantics { contentDescription = scope.spec.instruction },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val toPx = { o: Offset -> Offset(o.x * size.width, o.y * size.height) }
                val pts = guide.map { toPx(Offset(it.x, it.y)) }
                val path = Path().apply { moveTo(pts[0].x, pts[0].y); pts.drop(1).forEach { lineTo(it.x, it.y) } }
                val bold = scope.level >= Ladder.Hint
                drawPath(path, hue.tint, style = Stroke(46.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawPath(
                    path, if (bold) hue.lip else hue.fill,
                    style = Stroke((if (bold) 8.dp else 5.dp).toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = if (bold) null else PathEffect.dashPathEffect(floatArrayOf(18.dp.toPx(), 14.dp.toPx()))),
                )
                decorate(toPx)
                if (showEnds) {
                    drawCircle(hue.fill, 22.dp.toPx(), pts.first()); drawCircle(hue.lip, 22.dp.toPx(), pts.first(), style = Stroke(3.dp.toPx()))
                    val star = starPath(pts.last(), 26.dp.toPx())
                    drawPath(star, c.texasRose.fill); drawPath(star, c.texasRose.lip, style = Stroke(3.dp.toPx(), join = StrokeJoin.Round))
                }
                if (scope.level >= Ladder.Model) {
                    if (!scope.reducedMotion) drawCircle(c.action.fill, 16.dp.toPx(), pts[(runner.value * (pts.size - 1)).toInt()])
                    else for (i in listOf(pts.size / 4, pts.size / 2, 3 * pts.size / 4)) {
                        val a = pts[i]; val b = pts[(i + 2).coerceAtMost(pts.lastIndex)]; val len = hypot(b.x - a.x, b.y - a.y).coerceAtLeast(1f)
                        val d = (b - a) / len; val n = Offset(-d.y, d.x); val tip = a + d * 18.dp.toPx(); val back = tip - d * 18.dp.toPx()
                        drawPath(Path().apply { moveTo(tip.x, tip.y); lineTo(back.x + n.x * 12.dp.toPx(), back.y + n.y * 12.dp.toPx()); lineTo(back.x - n.x * 12.dp.toPx(), back.y - n.y * 12.dp.toPx()); close() }, c.action.fill)
                    }
                }
            }
            InkSurface(strokes, c.ink, widthDp = 9f, enabled = !scope.solved, onStroke = { s ->
                strokes += s
                scope.active()
                if (area.width == 0) return@InkSurface
                val score = traceScore(guide, strokes, aspect = area.width / area.height.toFloat())
                val end = s.points.lastOrNull()
                val at = end?.let { origin + Offset(it.x * area.width, it.y * area.height) }
                val detail = "coverage=${"%.2f".format(java.util.Locale.ROOT, score.coverage)};stray=${"%.2f".format(java.util.Locale.ROOT, score.stray)}"
                if (score.accepted) scope.respond(choice, true, s.tool, at, StrokeCodec.encode(strokes), detail)
                else {
                    val last = guide.last()
                    val reachedEnd = end != null && hypot((end.x - last.x) * area.width, (end.y - last.y) * area.height) < .1f * minOf(area.width, area.height)
                    if (reachedEnd || strokes.size >= 3) scope.respond(choice, false, s.tool, null, StrokeCodec.encode(strokes))
                }
            }, modifier = Modifier.fillMaxSize())
        }
        // Beside the pad, so the buttons never sit over the line.
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RoundIconButton("undo", ChildCopy.Undo, c.action, null) { if (strokes.isNotEmpty() && !scope.solved) strokes.removeAt(strokes.lastIndex) }
            RoundIconButton("clear", ChildCopy.Clear, c.action, null) { if (!scope.solved) strokes.clear() }
        }
    }
}

/** A smooth guide through [anchors] (0..1), sampled into [n] points, for letters and shapes. */
fun smoothGuide(anchors: List<Offset>, n: Int = 48): List<StrokePoint> {
    val path = com.nuha.app.ui.scene.smoothPath(anchors)
    val measure = androidx.compose.ui.graphics.PathMeasure().apply { setPath(path, false) }
    return List(n) { i -> measure.getPosition(measure.length * i / (n - 1)).let { StrokePoint(it.x, it.y, 0) } }
}
