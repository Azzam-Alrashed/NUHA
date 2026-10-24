package com.nuha.app.activities

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuha.app.DraftArabic
import com.nuha.app.Pending
import com.nuha.app.child.Activity
import com.nuha.app.child.activityHue
import com.nuha.app.task.ActivityPlan
import com.nuha.app.task.AnswerTile
import com.nuha.app.task.StrokePoint
import com.nuha.app.task.TaskScope
import com.nuha.app.task.TracePad
import com.nuha.app.task.TrialSpec
import com.nuha.app.ui.identity.Hue
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.theme.titleWeight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * «نسخ الأنماط الهندسية» (FR-PAT-001). Two separate modes: continuation (choose the next shape) and drawing (trace the
 * next shape). Which mode a session uses is PLACEHOLDER ([Pending.patternMode]: sessions alternate). Levels vary one
 * rule at a time: AB, then AAB, then ABC. Shapes differ by outline, not colour; the sequence reads right to left.
 * Draft content (docs/activities/patterns.md).
 */
@DraftArabic
internal val ShapeNames = mapOf("circle" to "دائرة", "triangle" to "مثلث", "square" to "مربع")

internal class PatternItem(val sequence: List<String>, val answer: String, val options: List<String>, val mode: String)

private val Rules = listOf(listOf(0, 1), listOf(0, 0, 1), listOf(0, 1, 2))

internal class PatternsPlan(completedSessions: Int) : ActivityPlan {
    // Sessions alternate modes, so each mode moves up a level every second session: one change at a time.
    private val level = Pending.levelFor(completedSessions / 2, top = 2)
    private val mode = Pending.patternMode(completedSessions)
    override val activity = Activity.Patterns
    override val taskVersion = 1
    override val difficulty = "level=$level;rule=${listOf("AB", "AAB", "ABC")[level]};mode=$mode"
    @DraftArabic
    override fun trials(seed: Long): List<TrialSpec> {
        val r = Random(seed)
        val rule = Rules[level]
        return List(slots) { i ->
            val shapes = ShapeNames.keys.shuffled(r)
            val full = List(rule.size * 2 + 1) { k -> shapes[rule[k % rule.size]] }
            val answer = full.last()
            val name = ShapeNames.getValue(answer)
            val item = PatternItem(full.dropLast(1), answer, ShapeNames.keys.shuffled(r), mode)
            if (mode == "draw") TrialSpec("draw-$i-$answer", "ارسم الشكل الذي يأتي بعد ذلك.", "أحسنت! بعد ذلك يأتي ${name}.", "انظر إلى النمط: بعد ذلك يأتي ${name}. ارسمه فوق الخط.", item)
            else TrialSpec("next-$i-$answer", "ما الشكل الذي يأتي بعد ذلك؟", "أحسنت! بعد ذلك يأتي ${name}.", "انظر إلى النمط: بعد ذلك يأتي ${name}. اضغط عليه.", item)
        }
    }
}

@Composable
internal fun PatternsContent(scope: TaskScope) {
    val c = LocalIdentity.current
    val hue = c.activityHue(Activity.Patterns)
    val item = scope.spec.item as PatternItem
    if (item.mode == "draw") {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Sequence(item, hue, cell = 72.dp, solved = scope.solved)
            TracePad(shapeGuide(item.answer), scope, hue, Modifier.fillMaxHeight().aspectRatio(1f), choice = item.answer)
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(40.dp)) {
            Sequence(item, hue, cell = 110.dp, solved = scope.solved)
            Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                for (o in item.options) AnswerTile(o, o == item.answer, scope, hue, ShapeNames.getValue(o), Modifier.size(170.dp)) {
                    ShapeGlyph(o, hue, Modifier.size(100.dp))
                }
            }
        }
    }
}

/** The pattern so far, then a dashed slot for the next shape (filled once solved). Reads right to left. */
@Composable
private fun Sequence(item: PatternItem, hue: Hue, cell: androidx.compose.ui.unit.Dp, solved: Boolean) {
    val c = LocalIdentity.current
    Row(
        Modifier
            .semantics(mergeDescendants = true) { contentDescription = item.sequence.joinToString("، ") { ShapeNames.getValue(it) } }
            .background(hue.tint, RoundedCornerShape(36.dp))
            .padding(horizontal = 28.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        for (s in item.sequence) ShapeGlyph(s, hue, Modifier.size(cell))
        Box(
            Modifier.size(cell + 12.dp).drawBehind {
                drawRoundRect(hue.lip, style = Stroke(4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12.dp.toPx(), 8.dp.toPx()))), cornerRadius = CornerRadius(20.dp.toPx()))
            },
            contentAlignment = Alignment.Center,
        ) {
            if (solved) ShapeGlyph(item.answer, hue, Modifier.size(cell))
            else Text("؟", color = c.ink, style = MaterialTheme.typography.displaySmall.titleWeight().copy(fontSize = 48.sp))
        }
    }
}

@Composable
internal fun ShapeGlyph(kind: String, hue: Hue, modifier: Modifier) {
    Canvas(modifier.clearAndSetSemantics {}) {
        val s = size.minDimension
        val stroke = Stroke(s * .06f, join = StrokeJoin.Round)
        when (kind) {
            "circle" -> { drawCircle(hue.fill, s * .4f); drawCircle(hue.lip, s * .4f, style = stroke) }
            "triangle" -> {
                val p = Path().apply { moveTo(s * .5f, s * .12f); lineTo(s * .9f, s * .86f); lineTo(s * .1f, s * .86f); close() }
                drawPath(p, hue.fill); drawPath(p, hue.lip, style = stroke)
            }
            else -> { drawRoundRect(hue.fill, Offset(s * .12f, s * .12f), Size(s * .76f, s * .76f), CornerRadius(s * .08f)); drawRoundRect(hue.lip, Offset(s * .12f, s * .12f), Size(s * .76f, s * .76f), CornerRadius(s * .08f), style = stroke) }
        }
    }
}

/** Tracing guides for the shapes, each starting at the top right and going leftward first, as writing does. */
internal fun shapeGuide(kind: String): List<StrokePoint> = when (kind) {
    "circle" -> List(49) { i -> val a = -PI / 4 - i * 2 * PI / 48; StrokePoint(.5f + .36f * cos(a).toFloat(), .5f + .36f * sin(a).toFloat(), 0) }
    "triangle" -> outline(listOf(.5f to .14f, .14f to .84f, .86f to .84f, .5f to .14f))
    else -> outline(listOf(.82f to .18f, .18f to .18f, .18f to .82f, .82f to .82f, .82f to .18f))
}

private fun outline(corners: List<Pair<Float, Float>>) = corners.zipWithNext { (ax, ay), (bx, by) ->
    List(16) { k -> StrokePoint(ax + (bx - ax) * k / 16f, ay + (by - ay) * k / 16f, 0) }
}.flatten() + corners.last().let { StrokePoint(it.first, it.second, 0) }

