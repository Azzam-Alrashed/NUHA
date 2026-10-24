package com.nuha.app.onboarding

import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuha.app.audio.Effects
import com.nuha.app.audio.Speech
import com.nuha.app.ui.identity.CheckBadge
import com.nuha.app.ui.identity.ChunkyButton
import com.nuha.app.ui.identity.ChunkySurface
import com.nuha.app.ui.identity.Hue
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.NuhaGuide
import com.nuha.app.ui.identity.Scene
import com.nuha.app.ui.identity.pressSink
import com.nuha.app.ui.theme.buttonWeight
import com.nuha.app.ui.theme.titleWeight

internal const val BUTTON_HEIGHT = 108
/** Centre of the primary button as a fraction of the width. The welcome guide stands here, so the button never moves between steps. */
internal const val PRIMARY_X = 531f / 640f
/** Distance from a downward bubble's right edge to its tail, so the tail can sit over the speaker. */
internal val BubbleTailInset = 92.dp
internal const val ONBOARDING_STEPS = 3

/**
 * Places the element so the point ([alignX], [alignY]) of its own box lands on ([x], [y]), in absolute
 * (left-based) coordinates. It reports zero size, so siblings in a Box are unaffected.
 */
internal fun Modifier.placeAt(x: Dp, y: Dp, alignX: Float = .5f, alignY: Float = .5f) = layout { measurable, constraints ->
    val p = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    layout(0, 0) { p.place((x.roundToPx() - alignX * p.width).toInt(), (y.roundToPx() - alignY * p.height).toInt()) }
}

/** The shared sky of every onboarding step: sun, soft hills and two flat clouds, in 640×400 sketch coordinates. */
internal fun DrawScope.drawSky(scene: Scene) {
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

internal enum class BubbleTail { Down, Right }

/**
 * Pill speech bubble. A [BubbleTail.Down] tail sits [BubbleTailInset] from the right edge; a [BubbleTail.Right]
 * tail points right from the middle, at a speaker beside the bubble. Tail sides are absolute, not mirrored.
 */
@Composable
internal fun SpeechBubble(text: String, modifier: Modifier, tail: BubbleTail = BubbleTail.Down, fontSize: TextUnit = 44.sp) {
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

/** Progress through onboarding: the current step is a wide pill, the others small dots. */
@Composable
internal fun StepDots(step: Int, modifier: Modifier = Modifier) {
    val c = LocalIdentity.current
    Row(
        modifier.semantics(mergeDescendants = true) { contentDescription = "الخطوة ${step + 1} من $ONBOARDING_STEPS" },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(ONBOARDING_STEPS) { i ->
            Box(Modifier.height(14.dp).width(if (i == step) 40.dp else 14.dp).background(if (i == step) c.action.fill else c.scene.tileLip, CircleShape))
        }
    }
}

/**
 * Chunky choice: white (or tint, when [selected]) face over a lip that thickens when selected, with a check badge
 * in the leading (right) corner so selection never relies on colour. Fires on touch-down like ChunkyButton.
 */
@Composable
internal fun ChoiceTile(
    hue: Hue,
    selected: Boolean,
    onSelect: () -> Unit,
    description: String,
    modifier: Modifier = Modifier,
    corner: Dp? = 28.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    var lastFired by remember { mutableLongStateOf(0L) }
    val fire by rememberUpdatedState(onSelect)
    val lip = if (selected) 8.dp else 5.dp
    ChunkySurface(
        hue, if (selected) hue.tint else Color.White,
        modifier
            .semantics(mergeDescendants = true) {
                role = Role.RadioButton
                this.selected = selected
                contentDescription = description
                onClick { fire(); true }
            }
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    pressed = true
                    val now = SystemClock.uptimeMillis()
                    if (now - lastFired >= 300) { lastFired = now; fire() }
                    tryAwaitRelease()
                    pressed = false
                })
            },
        lip = lip, maxLip = 8.dp, sink = pressSink(pressed, lip), corner = corner,
    ) {
        content()
        if (selected) CheckBadge(hue, Modifier.align(AbsoluteAlignment.TopRight).offset(x = 6.dp, y = (-6).dp))
    }
}

/** Soft round back control at the bottom-left; the arrow points right because the way back is rightward in RTL. */
@Composable
internal fun BackCircle(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalIdentity.current
    ChoiceTile(c.action, selected = false, onSelect = onBack, description = "رجوع", modifier = modifier.size(84.dp), corner = null) {
        Canvas(Modifier.size(36.dp).clearAndSetSemantics {}) {
            val s = size.width / 36f
            val stroke = Stroke(4.5f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
            drawLine(c.action.lip, Offset(6 * s, 18 * s), Offset(30 * s, 18 * s), stroke.width, StrokeCap.Round)
            drawPath(Path().apply { moveTo(20 * s, 8 * s); lineTo(30 * s, 18 * s); lineTo(20 * s, 28 * s) }, c.action.lip, style = stroke)
        }
    }
}

/**
 * Frame shared by onboarding steps 2 and 3: the sky, the host guide peeking over a white card with a speech
 * bubble beside it, and the bottom bar (back, step dots, and the primary button in the same place as on step 1).
 */
@Composable
internal fun OnboardingFrame(
    step: Int,
    prompt: String,
    primary: String,
    primaryEnabled: Boolean,
    onPrimary: () -> Unit,
    onBack: () -> Unit,
    speech: Speech?,
    effects: Effects?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = LocalIdentity.current
    BoxWithConstraints(Modifier.fillMaxSize().background(c.scene.ground), contentAlignment = AbsoluteAlignment.TopLeft) {
        val w = maxWidth
        Canvas(Modifier.fillMaxSize().clearAndSetSemantics {}) { drawSky(c.scene) }
        Column(
            Modifier
                .placeAt(w / 2, 196.dp, alignY = 0f)
                .width(minOf(1000.dp, w - 140.dp))
                .drawBehind {
                    val lip = 8.dp.toPx()
                    val face = Size(size.width, size.height - lip)
                    val r = CornerRadius(40.dp.toPx())
                    drawRoundRect(c.scene.tileLip, Offset(0f, lip), face, r)
                    drawRoundRect(Color.White, Offset.Zero, face, r)
                    // White card on white paper: outline it in high contrast so its edge stays visible.
                    if (c.highContrast) drawRoundRect(c.ink, Offset(1.dp.toPx(), 1.dp.toPx()), Size(face.width - 2.dp.toPx(), face.height - 2.dp.toPx()), r, style = Stroke(2.dp.toPx()))
                }
                .padding(start = 40.dp, end = 40.dp, top = 30.dp, bottom = 38.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            content = content,
        )
        NuhaGuide(Modifier.placeAt(w - 132.dp, 118.dp).size(190.dp), gaze = Offset(-1f, .4f), description = "نُهى", onTap = { speech?.say(prompt) })
        SpeechBubble(prompt, Modifier.placeAt(w - 232.dp, 104.dp, alignX = 1f), tail = BubbleTail.Right, fontSize = 36.sp)

        Box(Modifier.fillMaxSize().navigationBarsPadding().padding(bottom = 24.dp), contentAlignment = AbsoluteAlignment.BottomLeft) {
            BackCircle({ effects?.pop(); onBack() }, Modifier.placeAt(72.dp, -(BUTTON_HEIGHT / 2).dp))
            StepDots(step, Modifier.placeAt(w / 2, -(BUTTON_HEIGHT / 2).dp))
            ChunkyButton(
                primary,
                onClick = { effects?.pop(); onPrimary() },
                enabled = primaryEnabled,
                modifier = Modifier.placeAt(w * PRIMARY_X, 0.dp, alignY = 1f).height(BUTTON_HEIGHT.dp).widthIn(min = 320.dp),
                style = MaterialTheme.typography.displaySmall.buttonWeight().copy(fontSize = 40.sp, lineHeight = 52.sp),
            )
        }
    }
}

/** Section label inside an onboarding card. */
@Composable
internal fun CardLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge.titleWeight(), color = LocalIdentity.current.ink, modifier = Modifier.semantics { heading() })
}
