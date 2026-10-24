package com.nuha.app.ui.scene

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuha.app.ui.identity.Hue
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.Scene
import com.nuha.app.ui.theme.titleWeight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Scene pieces shared by onboarding and child mode: absolute placement, the sky, speech bubbles and roadside details.
// Sketch coordinates are a 640×400 box scaled to the screen, so the 16:10 table and tablets share one layout.

/** Distance from a downward bubble's right edge to its tail, so the tail can sit over the speaker. */
val BubbleTailInset = 92.dp
/**
 * Places the element so the point ([alignX], [alignY]) of its own box lands on ([x], [y]), in absolute
 * (left-based) coordinates. It reports zero size, so siblings in a Box are unaffected.
 */
fun Modifier.placeAt(x: Dp, y: Dp, alignX: Float = .5f, alignY: Float = .5f) = layout { measurable, constraints ->
    val p = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    layout(0, 0) { p.place((x.roundToPx() - alignX * p.width).toInt(), (y.roundToPx() - alignY * p.height).toInt()) }
}

/** The shared sky of child-mode scenes: sun, soft hills and two flat clouds, in 640×400 sketch coordinates. */
fun DrawScope.drawSky(scene: Scene) {
    fun p(x: Float, y: Float) = Offset(size.width * x / 640f, size.height * y / 400f)
    val u = size.width / 640f
    drawCircle(scene.sun, 46f * u, p(622f, 14f))
    drawOval(scene.hill, Offset(-140f * u, 330f * u), Size(400f * u, 200f * u))
    drawOval(scene.hill, Offset(170f * u, 360f * u), Size(300f * u, 160f * u))
    cloud(scene.cloud, p(214f, 62f), 20f * u)
    cloud(scene.cloud, p(318f, 40f), 13f * u)
}

private fun DrawScope.cloud(color: Color, center: Offset, r: Float) {
    drawRoundRect(color, center + Offset(-1.6f * r, -.2f * r), Size(3.2f * r, 1.2f * r), CornerRadius(.6f * r))
    drawCircle(color, .75f * r, center + Offset(-.6f * r, -.1f * r))
    drawCircle(color, r, center + Offset(.4f * r, -.35f * r))
}

enum class BubbleTail { Down, Right }

/**
 * Pill speech bubble. A [BubbleTail.Down] tail sits [BubbleTailInset] from the right edge; a [BubbleTail.Right]
 * tail points right from the middle, at a speaker beside the bubble. Tail sides are absolute, not mirrored.
 */
@Composable
fun SpeechBubble(text: String, modifier: Modifier, tail: BubbleTail = BubbleTail.Down, fontSize: TextUnit = 44.sp) {
    val c = LocalIdentity.current
    val tailSize = 26.dp
    Box(
        modifier
            .drawBehind {
                val t = tailSize.toPx()
                val body = if (tail == BubbleTail.Down) Size(size.width, size.height - t) else Size(size.width - t, size.height)
                drawRoundRect(c.scene.bubble, Offset.Zero, body, CornerRadius(body.height / 2))
                val half = 20.dp.toPx()
                drawPath(Path().apply {
                    if (tail == BubbleTail.Down) {
                        val x = size.width - BubbleTailInset.toPx()
                        moveTo(x - half, body.height - 1f); lineTo(x, size.height); lineTo(x + half, body.height - 1f)
                    } else {
                        val y = body.height / 2
                        moveTo(body.width - 1f, y - half * .8f); lineTo(size.width, y); lineTo(body.width - 1f, y + half * .8f)
                    }
                    close()
                }, c.scene.bubble)
            }
            .absolutePadding(
                left = 40.dp, right = 40.dp + if (tail == BubbleTail.Right) tailSize else 0.dp,
                top = 10.dp, bottom = 10.dp + if (tail == BubbleTail.Down) tailSize else 0.dp,
            )
            .semantics { heading() },
    ) {
        Text(text, style = MaterialTheme.typography.displaySmall.titleWeight().copy(fontSize = fontSize, lineHeight = fontSize * 1.5f), color = c.ink)
    }
}

fun DrawScope.bush(hue: Hue, center: Offset, r: Float) {
    for ((dx, dy, k) in listOf(Triple(-.8f, .25f, .75f), Triple(.8f, .25f, .75f), Triple(0f, 0f, 1f))) {
        drawCircle(hue.lip, k * r, center + Offset(dx * r, dy * r + .12f * r))
        drawCircle(hue.fill, k * r, center + Offset(dx * r, dy * r))
    }
}

fun DrawScope.bloom(petal: Hue, heart: Color, center: Offset, r: Float) {
    for (i in 0 until 5) {
        val a = i * 2 * PI / 5 - PI / 2
        drawCircle(petal.fill, r, center + Offset((cos(a) * r).toFloat(), (sin(a) * r).toFloat()))
    }
    drawCircle(heart, .6f * r, center)
}

fun DrawScope.star(color: Color, center: Offset, r: Float) {
    val path = Path()
    for (i in 0 until 10) {
        val a = i * PI / 5 - PI / 2
        val k = if (i % 2 == 0) r else r * .48f
        val pt = center + Offset((cos(a) * k).toFloat(), (sin(a) * k).toFloat())
        if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
    }
    path.close()
    drawPath(path, color)
}

/** A smooth road through [points] (Catmull-Rom turned into cubic Béziers), so stops can be moved without redrawing curves. */
fun smoothPath(points: List<Offset>): Path = Path().apply {
    moveTo(points[0].x, points[0].y)
    for (i in 0 until points.size - 1) {
        val p0 = points[maxOf(i - 1, 0)]; val p1 = points[i]; val p2 = points[i + 1]; val p3 = points[minOf(i + 2, points.size - 1)]
        val c1 = p1 + (p2 - p0) / 6f
        val c2 = p2 - (p3 - p1) / 6f
        cubicTo(c1.x, c1.y, c2.x, c2.y, p2.x, p2.y)
    }
}
