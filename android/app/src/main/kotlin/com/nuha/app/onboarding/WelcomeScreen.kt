package com.nuha.app.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuha.app.audio.rememberEffects
import com.nuha.app.audio.rememberSpeech
import com.nuha.app.ui.identity.ChunkyButton
import com.nuha.app.ui.identity.Hue
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.LocalReducedMotion
import com.nuha.app.ui.identity.NuhaGuide
import com.nuha.app.ui.identity.NuhaLogo
import com.nuha.app.ui.identity.NuhaPictogram
import com.nuha.app.ui.theme.buttonWeight
import com.nuha.app.ui.theme.titleWeight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

private const val GREETING = "أهلاً! أنا نُهى"
/** Quiet time before the guide looks at the start button and the button bobs once. */
private const val INVITE_AFTER_MILLIS = 5000L
private const val GUIDE_SIZE = 280

/**
 * Onboarding, screen 1: the child meets the flower guide at the start of the road. One action starts.
 * Positions are fractions of the screen, from the 640×400 sketch, so the 16:10 table and tablets share one layout.
 *
 * Motion (off under reduced motion): the guide pops in, the bubble grows from its tail, then the button
 * rises in. After a quiet spell the guide looks at the button and it bobs once. Starting sends the guide
 * hopping along the road to the first stop before [onStart].
 */
@Composable
fun WelcomeScreen(onStart: () -> Unit, voice: Boolean, effects: Boolean, modifier: Modifier = Modifier) {
    val c = LocalIdentity.current
    val reduced = LocalReducedMotion.current
    val speech = rememberSpeech(voice)
    val sounds = rememberEffects(effects)
    val scope = rememberCoroutineScope()

    val bubbleIn = remember { Animatable(if (reduced) 1f else 0f) }
    val buttonIn = remember { Animatable(if (reduced) 1f else 0f) }
    val bob = remember { Animatable(0f) }
    val walk = remember { Animatable(0f) }
    val gaze = remember { Animatable(Offset(-1f, .3f), Offset.VectorConverter) }
    var leaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch { delay(if (reduced) 0 else 250); bubbleIn.animateTo(1f, spring(dampingRatio = .6f, stiffness = Spring.StiffnessMediumLow)) }
        launch { delay(if (reduced) 0 else 450); buttonIn.animateTo(1f, tween(if (reduced) 0 else 260)) }
        delay(if (reduced) 0 else 400)
        speech?.say(GREETING)
        delay(INVITE_AFTER_MILLIS)
        if (!reduced && !leaving) {
            gaze.animateTo(Offset(0f, 1f), tween(300))
            bob.animateTo(1f, tween(160))
            bob.animateTo(0f, spring(dampingRatio = .4f, stiffness = Spring.StiffnessMediumLow))
        }
    }

    // Scene positions are absolute (left-based) so the road reads right-to-left whatever the layout direction.
    BoxWithConstraints(modifier.fillMaxSize().background(c.scene.ground), contentAlignment = AbsoluteAlignment.TopLeft) {
        val w = maxWidth
        val h = maxHeight
        fun x(f: Float) = w * (f / 640f)
        fun y(f: Float) = h * (f / 400f)
        Scenery(Modifier.fillMaxSize())
        // The logo sits in the sky just inside the sun, clear of the clouds and the guide's bubble.
        NuhaLogo(
            Modifier.placeAt(x(566f), y(34f), alignX = 1f).graphicsLayer { alpha = bubbleIn.value.coerceIn(0f, 1f) },
            style = MaterialTheme.typography.displaySmall.titleWeight(),
        )

        val stop1 = Offset(357f, 232f)
        RoadStop(c.emotions, "emotions", 120.dp, Modifier.placeAt(x(44f), y(96f)))
        RoadStop(c.matching, "matching", 132.dp, Modifier.placeAt(x(148f), y(238f)))
        RoadStop(c.letters, "letters", 152.dp, Modifier.placeAt(x(stop1.x), y(stop1.y)))

        // The guide stands where the road enters the screen; its feet sit on the road.
        val guideX = x(531f)
        val guideY = y(234f)
        val tip = guideY - (GUIDE_SIZE / 2).dp + 18.dp
        val walkTo = Offset((x(stop1.x) - guideX).value, (y(stop1.y) - 96.dp - guideY).value)
        NuhaGuide(
            Modifier
                .placeAt(guideX, guideY)
                .size(GUIDE_SIZE.dp)
                .graphicsLayer {
                    val t = walk.value
                    translationX = walkTo.x.dp.toPx() * t
                    translationY = walkTo.y.dp.toPx() * t - abs(sin(t * 2 * PI)).toFloat() * 48.dp.toPx()
                },
            gaze = gaze.value,
            description = "نُهى، زهرة مبتسمة",
            onTap = { speech?.say(GREETING) },
        )
        SpeechBubble(
            GREETING,
            Modifier
                .placeAt(guideX + BubbleTailInset, tip, alignX = 1f, alignY = 1f)
                .graphicsLayer {
                    val v = bubbleIn.value
                    // Fades out in the first third of the walk so the bubble doesn't stay behind.
                    alpha = (v * 2f).coerceIn(0f, 1f) * (1f - walk.value * 3f).coerceIn(0f, 1f)
                    scaleX = .4f + .6f * v; scaleY = .4f + .6f * v
                    transformOrigin = TransformOrigin(if (size.width > 0f) 1f - BubbleTailInset.toPx() / size.width else .5f, 1f)
                },
        )

        val bobPx = with(LocalDensity.current) { 14.dp.toPx() }
        Box(Modifier.fillMaxSize().navigationBarsPadding().padding(bottom = 24.dp), contentAlignment = AbsoluteAlignment.BottomLeft) {
            StepDots(0, Modifier.placeAt(w / 2, -(BUTTON_HEIGHT / 2).dp).graphicsLayer { alpha = buttonIn.value })
            ChunkyButton(
                "هيّا نبدأ",
                onClick = {
                    if (!leaving) {
                        leaving = true
                        sounds?.pop()
                        scope.launch {
                            if (!reduced) {
                                gaze.snapTo(Offset(-1f, 0f))
                                walk.animateTo(1f, tween(900, easing = LinearEasing))
                            }
                            onStart()
                        }
                    }
                },
                modifier = Modifier
                    .placeAt(w * PRIMARY_X, 0.dp, alignY = 1f)
                    .height(BUTTON_HEIGHT.dp)
                    .widthIn(min = 320.dp)
                    .graphicsLayer {
                        alpha = buttonIn.value
                        translationY = (1f - buttonIn.value) * 24.dp.toPx() - bob.value * bobPx
                    },
                style = MaterialTheme.typography.displaySmall.buttonWeight().copy(fontSize = 40.sp, lineHeight = 52.sp),
            )
        }
    }
}

/** The sky, the road and roadside details: everything decorative, drawn once in sketch coordinates. */
@Composable
private fun Scenery(modifier: Modifier) {
    val c = LocalIdentity.current
    val scene = c.scene
    Canvas(modifier.clearAndSetSemantics {}) {
        fun p(x: Float, y: Float) = Offset(size.width * x / 640f, size.height * y / 400f)
        val u = size.width / 640f
        drawSky(scene)

        val road = Path().apply {
            p(660f, 330f).let { moveTo(it.x, it.y) }
            for ((a, b, e) in listOf(
                Triple(p(520f, 330f), p(470f, 230f), p(360f, 240f)),
                Triple(p(250f, 250f), p(220f, 330f), p(150f, 250f)),
                Triple(p(80f, 170f), p(120f, 110f), p(-20f, 110f)),
            )) cubicTo(a.x, a.y, b.x, b.y, e.x, e.y)
        }
        drawPath(road, scene.road, style = Stroke(50f * u, cap = StrokeCap.Round))
        drawPath(road, scene.dash, style = Stroke(5f * u, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f * u, 20f * u))))

        // Roadside details, kept off the road, the bubble and the button.
        bush(c.pastelGreen, p(236f, 160f), 16f * u)
        bush(c.pastelGreen, p(410f, 328f), 13f * u)
        bloom(c.illusion, c.texasRose.fill, p(70f, 228f), 6f * u)
        bloom(c.monaLisa, c.texasRose.fill, p(270f, 176f), 5f * u)
        bloom(c.illusion, c.texasRose.fill, p(440f, 186f), 5f * u)
        for ((pt, r) in listOf(p(196f, 362f) to 6f, p(214f, 368f) to 4f, p(512f, 150f) to 5f)) drawOval(scene.tileLip, pt - Offset(r * u, r * .6f * u), Size(2 * r * u, 1.2f * r * u))
    }
}

private fun DrawScope.bush(hue: Hue, center: Offset, r: Float) {
    for ((dx, dy, k) in listOf(Triple(-.8f, .25f, .75f), Triple(.8f, .25f, .75f), Triple(0f, 0f, 1f))) {
        drawCircle(hue.lip, k * r, center + Offset(dx * r, dy * r + .12f * r))
        drawCircle(hue.fill, k * r, center + Offset(dx * r, dy * r))
    }
}

private fun DrawScope.bloom(petal: Hue, heart: Color, center: Offset, r: Float) {
    for (i in 0 until 5) {
        val a = i * 2 * PI / 5 - PI / 2
        drawCircle(petal.fill, r, center + Offset((kotlin.math.cos(a) * r).toFloat(), (sin(a) * r).toFloat()))
    }
    drawCircle(heart, .6f * r, center)
}

/** A white chunky tile on the road: an activity's pictogram in its hue over three empty stars to earn. Decorative here. */
@Composable
private fun RoadStop(hue: Hue, kind: String, size: Dp, modifier: Modifier) {
    val scene = LocalIdentity.current.scene
    val lip = 6.dp
    Box(
        modifier
            .size(size)
            .clearAndSetSemantics {}
            .drawBehind {
                val face = Size(this.size.width, this.size.height - lip.toPx())
                val r = CornerRadius(face.width * .3f)
                drawRoundRect(scene.tileLip, Offset(0f, lip.toPx()), face, r)
                drawRoundRect(Color.White, Offset.Zero, face, r)
                val star = face.width * .075f
                for (i in -1..1) star(scene.tileLip, Offset(face.width / 2 + i * star * 2.6f, face.height * .83f), star)
            },
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            Modifier.offset(y = size * .13f).size(size * .5f).background(hue.fill, RoundedCornerShape(size * .15f)),
            contentAlignment = Alignment.Center,
        ) { NuhaPictogram(kind, hue.onFill, Modifier.size(size * .38f)) }
    }
}

private fun DrawScope.star(color: Color, center: Offset, r: Float) {
    val path = Path()
    for (i in 0 until 10) {
        val a = i * PI / 5 - PI / 2
        val k = if (i % 2 == 0) r else r * .48f
        val pt = center + Offset((kotlin.math.cos(a) * k).toFloat(), (sin(a) * k).toFloat())
        if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
    }
    path.close()
    drawPath(path, color)
}
