package com.nuha.app.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuha.app.ui.identity.ChunkyButton
import com.nuha.app.ui.identity.Hue
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.NuhaGuide
import com.nuha.app.ui.identity.NuhaPictogram
import com.nuha.app.ui.theme.buttonWeight
import com.nuha.app.ui.theme.titleWeight
import kotlinx.coroutines.launch

/** How long an adult holds the corner gate to open settings. Long presses are not used anywhere in child mode. */
private const val GATE_HOLD_MILLIS = 3000

/**
 * Onboarding, screen 1: the child meets the flower guide at the start of the road.
 * One action starts; adults reach settings through the held gate in the corner.
 * Positions are fractions of the screen, from the 640×400 sketch, so the 16:10 table and tablets share one layout.
 */
@Composable
fun WelcomeScreen(onStart: () -> Unit, onOpenAdult: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalIdentity.current
    // Scene positions are absolute (left-based) so the road reads right-to-left whatever the layout direction.
    BoxWithConstraints(modifier.fillMaxSize().background(c.scene.ground), contentAlignment = AbsoluteAlignment.TopLeft) {
        val w = maxWidth
        val h = maxHeight
        Road(Modifier.fillMaxSize())
        // Later stops fade so the first one reads as "here"; the road itself is decorative.
        RoadStop(c.emotions, "emotions", 128.dp, .4f, Modifier.centeredAt(w * (42f / 640), h * (102f / 400), 128.dp))
        RoadStop(c.matching, "matching", 140.dp, .55f, Modifier.centeredAt(w * (147f / 640), h * (243f / 400), 140.dp))
        RoadStop(c.letters, "letters", 156.dp, 1f, Modifier.centeredAt(w * (357f / 640), h * (239f / 400), 156.dp))

        val guideX = w * (545f / 640)
        val guideY = h * (255f / 400)
        // Voice greeting replays on tap once recordings exist; for now the tap only hops.
        NuhaGuide(Modifier.centeredAt(guideX, guideY, 300.dp), gaze = Offset(-1f, .3f), description = "نُهى، زهرة مبتسمة", onTap = {})
        val bubbleRight = w * (26f / 640)
        SpeechBubble(
            "أهلاً! أنا نُهى",
            tailFromRight = w - bubbleRight - guideX,
            modifier = Modifier.align(AbsoluteAlignment.TopRight).absolutePadding(right = bubbleRight, top = guideY - 150.dp + 14.dp - 152.dp),
        )

        Box(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
            AdultGate(onOpenAdult, Modifier.align(AbsoluteAlignment.TopLeft))
            ChunkyButton(
                "هيّا نبدأ", onStart,
                Modifier.align(Alignment.BottomCenter).height(112.dp).widthIn(min = 340.dp),
                style = MaterialTheme.typography.displaySmall.buttonWeight().copy(fontSize = 40.sp, lineHeight = 52.sp),
            )
        }
    }
}

private fun Modifier.centeredAt(x: Dp, y: Dp, size: Dp) = absoluteOffset(x - size / 2, y - size / 2).size(size)

/** Thick road with white dashes, from off-screen right (where reading starts in RTL) toward the top left. */
@Composable
private fun Road(modifier: Modifier) {
    val scene = LocalIdentity.current.scene
    Canvas(modifier.clearAndSetSemantics {}) {
        fun p(x: Float, y: Float) = Offset(size.width * x / 640f, size.height * y / 400f)
        drawCircle(scene.glow, 140.dp.toPx(), p(80f, 360f))
        drawCircle(scene.glow, 100.dp.toPx(), p(600f, 40f))
        val road = Path().apply {
            p(660f, 330f).let { moveTo(it.x, it.y) }
            for ((a, b, e) in listOf(
                Triple(p(520f, 330f), p(470f, 230f), p(360f, 240f)),
                Triple(p(250f, 250f), p(220f, 330f), p(150f, 250f)),
                Triple(p(80f, 170f), p(120f, 110f), p(-20f, 110f)),
            )) cubicTo(a.x, a.y, b.x, b.y, e.x, e.y)
        }
        drawPath(road, scene.road, style = Stroke(100.dp.toPx(), cap = StrokeCap.Round))
        drawPath(road, scene.dash, style = Stroke(10.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(28.dp.toPx(), 40.dp.toPx()))))
    }
}

/** A white chunky tile on the road holding an activity's pictogram in its hue; [alpha] fades only the pictogram. Decorative here. */
@Composable
private fun RoadStop(hue: Hue, kind: String, size: Dp, alpha: Float, modifier: Modifier) {
    val scene = LocalIdentity.current.scene
    val lip = 6.dp
    Box(
        modifier
            .clearAndSetSemantics {}
            .drawBehind {
                val face = Size(this.size.width, this.size.height - lip.toPx())
                val r = CornerRadius(face.width * .3f)
                drawRoundRect(scene.tileLip, Offset(0f, lip.toPx()), face, r)
                drawRoundRect(Color.White, Offset.Zero, face, r)
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.padding(bottom = lip).size(size * .52f).graphicsLayer { this.alpha = alpha }.background(hue.fill, RoundedCornerShape(size * .15f)),
            contentAlignment = Alignment.Center,
        ) { NuhaPictogram(kind, hue.onFill, Modifier.size(size * .4f)) }
    }
}

/** White pill bubble whose tail sits [tailFromRight] from its right edge, pointing down at the speaker. */
@Composable
private fun SpeechBubble(text: String, tailFromRight: Dp, modifier: Modifier) {
    val c = LocalIdentity.current
    val tail = 28.dp
    Box(
        modifier
            .drawBehind {
                val body = Size(size.width, size.height - tail.toPx())
                drawRoundRect(Color.White, Offset.Zero, body, CornerRadius(body.height / 2))
                val x = (size.width - tailFromRight.toPx()).coerceIn(body.height / 2, size.width - body.height / 2)
                drawPath(Path().apply {
                    moveTo(x - 22.dp.toPx(), body.height - 1f)
                    lineTo(x, size.height)
                    lineTo(x + 22.dp.toPx(), body.height - 1f)
                    close()
                }, Color.White)
            }
            .padding(start = 44.dp, end = 44.dp, top = 14.dp, bottom = 14.dp + tail)
            .semantics { heading() },
    ) {
        Text(text, style = MaterialTheme.typography.displaySmall.titleWeight().copy(fontSize = 52.sp, lineHeight = 76.sp), color = c.ink)
    }
}

/** Adult-only corner control: hold for [GATE_HOLD_MILLIS] while a ring fills. TalkBack users get a direct action. */
@Composable
private fun AdultGate(onOpen: () -> Unit, modifier: Modifier) {
    val c = LocalIdentity.current
    val open by rememberUpdatedState(onOpen)
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    Box(
        modifier
            .size(80.dp)
            .semantics {
                role = Role.Button
                contentDescription = "إعدادات الكبار. اضغط مطوّلاً ثلاث ثوانٍ"
                onClick { open(); true }
            }
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    val hold = scope.launch {
                        progress.animateTo(1f, tween(GATE_HOLD_MILLIS, easing = LinearEasing))
                        open()
                    }
                    tryAwaitRelease()
                    hold.cancel()
                    progress.snapTo(0f)
                })
            }
            .drawBehind {
                drawCircle(c.scene.gate)
                val ring = 6.dp.toPx()
                if (progress.value > 0f) drawArc(
                    c.scene.gateIcon, -90f, 360f * progress.value, false,
                    Offset(ring / 2, ring / 2), Size(size.width - ring, size.height - ring), style = Stroke(ring, cap = StrokeCap.Round),
                )
                // Padlock: body and shackle.
                val u = size.width / 80f
                drawRoundRect(c.scene.gateIcon, Offset(26 * u, 38 * u), Size(28 * u, 22 * u), CornerRadius(6 * u))
                drawArc(c.scene.gateIcon, 180f, 180f, false, Offset(30 * u, 24 * u), Size(20 * u, 24 * u), style = Stroke(5 * u))
                drawLine(c.scene.gateIcon, Offset(30 * u, 36 * u), Offset(30 * u, 40 * u), 5 * u)
                drawLine(c.scene.gateIcon, Offset(50 * u, 36 * u), Offset(50 * u, 40 * u), 5 * u)
            },
    )
}
