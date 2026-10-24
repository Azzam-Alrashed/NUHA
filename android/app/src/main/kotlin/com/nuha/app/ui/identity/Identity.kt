package com.nuha.app.ui.identity

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import kotlin.math.*
import kotlinx.coroutines.launch

/** One candy hue in the five roles of docs/research/ui-style.md (Palette). */
@Immutable
data class Hue(
    val fill: Color,
    val lip: Color,
    val onFill: Color,
    val deep: Color,
    val tint: Color,
    /** onFill passes only at WCAG large-text size (at least 18.66px bold). */
    val onFillLargeOnly: Boolean = false,
)

/** Colours of an illustrated scene (the road), kept apart from the hues so each mode can calm it separately. */
@Immutable
data class Scene(
    val ground: Color,
    val road: Color,
    val dash: Color,
    val tileLip: Color,
    val sun: Color,
    val hill: Color,
    val cloud: Color,
    /** Speech bubbles; white except in high contrast, where white would vanish on white paper. */
    val bubble: Color = Color.White,
)

/**
 * Brand tokens shared by illustrations and onboarding components.
 * tools/android/check_identity.py checks every text and boundary pair in all three modes.
 */
@Immutable
data class IdentityPalette(
    val ink: Color,
    val paper: Color,
    val scene: Scene,
    val amethyst: Hue,
    val slateBlue: Hue,
    val summerSky: Hue,
    val downy: Hue,
    val pastelGreen: Hue,
    val texasRose: Hue,
    val monaLisa: Hue,
    val illusion: Hue,
    val highContrast: Boolean = false,
) {
    // One hue per role, kept everywhere it appears (ui-style.md, Palette).
    val action: Hue get() = amethyst
    val settings: Hue get() = downy
    val guide: Hue get() = monaLisa
    val road: Hue get() = texasRose
    val matching: Hue get() = pastelGreen
    val letters: Hue get() = summerSky
    val emotions: Hue get() = illusion
    /** Top of the soft-flat face gradient, at most ~6% lighter. Off in high contrast, where it would cut white-text contrast below 4.5:1. */
    val lift: Float get() = if (highContrast) 0f else .08f
}

private const val INK = 0xFF1E2A4A
private const val WHITE = 0xFFFFFFFF
private fun hue(fill: Long, lip: Long, onFill: Long, deep: Long, tint: Long, largeOnly: Boolean = false) =
    Hue(Color(fill), Color(lip), Color(onFill), Color(deep), Color(tint), largeOnly)

// The doc's table. Lips are at least 3:1 against paper so tile edges stay identifiable as controls.
private val Colourful = IdentityPalette(
    ink = Color(0xFF1E2A4A),
    paper = Color(0xFFFFF8EE),
    // Sunny ground of the Ladybug road on the moodboard.
    scene = Scene(Color(0xFFFFD866), Color(0xFFFFEDB0), Color.White, Color(0xFFEADBA8), Color(0xFFFFB356), Color(0xFFFFCB45), Color.White.copy(alpha = .9f)),
    amethyst = hue(0xFF9956DE, 0xFF7340A6, WHITE, 0xFF9755DC, 0xFFEBDDF8, largeOnly = true),
    slateBlue = hue(0xFF7274ED, 0xFF5657B2, WHITE, 0xFF686AD8, 0xFFE3E3FB, largeOnly = true),
    summerSky = hue(0xFF1FA7E1, 0xFF1D9ACF, INK, 0xFF187FAB, 0xFFD2EDF9),
    downy = hue(0xFF6ED1CF, 0xFF519B99, INK, 0xFF437F7E, 0xFFE2F6F5),
    pastelGreen = hue(0xFF75D06A, 0xFF5AA052, INK, 0xFF4A8343, 0xFFE3F6E1),
    texasRose = hue(0xFFFFB356, 0xFFBD8440, INK, 0xFF9C6D34, 0xFFFFF0DD),
    monaLisa = hue(0xFFFF8B8B, 0xFFD47373, INK, 0xFFAD5F5F, 0xFFFFE8E8),
    illusion = hue(0xFFFB96BB, 0xFFC97896, INK, 0xFFA3627A, 0xFFFEEAF1),
)

// Each fill moved 55% toward its tint (sRGB mix); lips, deep shades and tints unchanged; ink text on every fill.
private val Muted = Colourful.copy(
    scene = Scene(Color(0xFFFFEDBE), Color(0xFFFFF8E8), Color.White, Color(0xFFE6DCC0), Color(0xFFFFD9A8), Color(0xFFFFE4A3), Color.White),
    amethyst = hue(0xFFC6A0EC, 0xFF7340A6, INK, 0xFF9755DC, 0xFFEBDDF8),
    slateBlue = hue(0xFFB0B1F5, 0xFF5657B2, INK, 0xFF686AD8, 0xFFE3E3FB),
    summerSky = hue(0xFF81CEEE, 0xFF1D9ACF, INK, 0xFF187FAB, 0xFFD2EDF9),
    downy = hue(0xFFAEE5E4, 0xFF519B99, INK, 0xFF437F7E, 0xFFE2F6F5),
    pastelGreen = hue(0xFFB2E5AB, 0xFF5AA052, INK, 0xFF4A8343, 0xFFE3F6E1),
    texasRose = hue(0xFFFFD5A0, 0xFFBD8440, INK, 0xFF9C6D34, 0xFFFFF0DD),
    monaLisa = hue(0xFFFFBEBE, 0xFFD47373, INK, 0xFFAD5F5F, 0xFFFFE8E8),
    illusion = hue(0xFFFDC4D9, 0xFFC97896, INK, 0xFFA3627A, 0xFFFEEAF1),
)

// Deep shades as fills with white text, ink lips, white tints on white paper.
private val HighContrast = IdentityPalette(
    ink = Color(0xFF1E2A4A),
    paper = Color(0xFFFFFFFF),
    scene = Scene(Color.White, Color(0xFFE4E7EE), Color(0xFF1E2A4A), Color(0xFF1E2A4A), Color(0xFFE4E7EE), Color(0xFFEEF0F4), Color(0xFFE4E7EE), bubble = Color(0xFFE4E7EE)),
    amethyst = hue(0xFF9755DC, INK, WHITE, 0xFF9755DC, WHITE),
    slateBlue = hue(0xFF686AD8, INK, WHITE, 0xFF686AD8, WHITE),
    summerSky = hue(0xFF187FAB, INK, WHITE, 0xFF187FAB, WHITE),
    downy = hue(0xFF437F7E, INK, WHITE, 0xFF437F7E, WHITE),
    pastelGreen = hue(0xFF4A8343, INK, WHITE, 0xFF4A8343, WHITE),
    texasRose = hue(0xFF9C6D34, INK, WHITE, 0xFF9C6D34, WHITE),
    monaLisa = hue(0xFFAD5F5F, INK, WHITE, 0xFFAD5F5F, WHITE),
    illusion = hue(0xFFA3627A, INK, WHITE, 0xFFA3627A, WHITE),
    highContrast = true,
)

val LocalReducedMotion = staticCompositionLocalOf { true }
val LocalIdentity = staticCompositionLocalOf { Colourful }
fun paletteFor(mode: String) = when (mode) {
    "muted" -> Muted
    "contrast" -> HighContrast
    else -> Colourful
}

/** WCAG 2.2 contrast ratio. */
fun contrast(a: Color, b: Color): Float {
    val la = a.luminance(); val lb = b.luminance()
    return (max(la, lb) + .05f) / (min(la, lb) + .05f)
}

/** Mixes toward white in sRGB, matching the tint arithmetic in check_identity.py. */
fun Color.lifted(t: Float) = Color(red + (1 - red) * t, green + (1 - green) * t, blue + (1 - blue) * t, alpha)

/**
 * Soft-flat chunky surface: a face over a darker lip, with one soft hue-tinted shadow.
 * The box reserves [maxLip] so a thicker lip or a [sink] never changes layout or the hit area.
 * A face under 3:1 against paper also gets a 2dp edge in the lip colour (WCAG 1.4.11).
 */
@Composable
fun ChunkySurface(
    hue: Hue,
    face: Color,
    modifier: Modifier = Modifier,
    lip: Dp = 5.dp,
    maxLip: Dp = lip,
    sink: Dp = 0.dp,
    corner: Dp? = 24.dp,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    val c = LocalIdentity.current
    val edge = contrast(face, c.paper) < 3f
    val shape = if (corner == null) RoundedCornerShape(50) else RoundedCornerShape(corner)
    Box(
        propagateMinConstraints = true,
        modifier = modifier
            .dropShadow(shape, Shadow(radius = 14.dp, color = hue.lip, offset = DpOffset(0.dp, 6.dp), alpha = .22f))
            .drawBehind {
                val faceH = size.height - maxLip.toPx()
                val r = CornerRadius(corner?.toPx() ?: (faceH / 2))
                drawRoundRect(hue.lip, Offset(0f, lip.toPx()), Size(size.width, faceH), r)
                translate(top = sink.toPx()) {
                    drawRoundRect(Brush.verticalGradient(listOf(face.lifted(c.lift), face), 0f, faceH), Offset.Zero, Size(size.width, faceH), r)
                    if (edge) {
                        val w = 2.dp.toPx()
                        drawRoundRect(hue.lip, Offset(w / 2, w / 2), Size(size.width - w, faceH - w), CornerRadius(r.x - w / 2), style = Stroke(w))
                    }
                }
            },
    ) {
        Box(Modifier.padding(bottom = maxLip).graphicsLayer { translationY = sink.toPx() }, contentAlignment = contentAlignment, content = content)
    }
}

/** The face sinks by the lip height while pressed; instant under reduced motion. */
@Composable
fun pressSink(pressed: Boolean, lip: Dp): Dp {
    val reduced = LocalReducedMotion.current
    val sink by animateDpAsState(
        if (pressed) lip else 0.dp,
        if (reduced) snap() else spring(dampingRatio = .5f, stiffness = Spring.StiffnessHigh),
        label = "Press sink",
    )
    return sink
}

/**
 * Chunky pill button. The tap fires on touch-down (debounced), the face sinks by the lip height,
 * and neither the hit area nor neighbouring layout moves. Primary: hue fill; secondary: hue tint. Disabled: the tint
 * face with faint ink, rather than fading the whole button into whatever lies behind it.
 */
@Composable
fun ChunkyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hue: Hue = LocalIdentity.current.action,
    secondary: Boolean = false,
    enabled: Boolean = true,
    style: TextStyle = TextStyle.Default,
    trailing: String? = null,
) {
    val c = LocalIdentity.current
    val lip = if (secondary) 5.dp else 6.dp
    var pressed by remember { mutableStateOf(false) }
    var lastFired by remember { mutableLongStateOf(0L) }
    val fire by rememberUpdatedState(onClick)
    val sink = pressSink(pressed, lip)
    val ink = when { !enabled -> c.ink.copy(alpha = .38f); secondary -> c.ink; else -> hue.onFill }
    ChunkySurface(
        hue, if (secondary || !enabled) hue.tint else hue.fill,
        modifier
            .heightIn(min = 72.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                onClick { if (enabled) fire(); enabled }
                if (!enabled) disabled()
            }
            .pointerInput(enabled) {
                if (enabled) detectTapGestures(onPress = {
                    pressed = true
                    val now = SystemClock.uptimeMillis()
                    if (now - lastFired >= 300) { lastFired = now; fire() }
                    tryAwaitRelease()
                    pressed = false
                })
            },
        lip = lip, sink = sink, corner = null,
    ) {
        Row(Modifier.padding(horizontal = 28.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text, color = ink, style = style)
            if (trailing != null) {
                Spacer(Modifier.width(20.dp))
                Text(trailing, color = ink, style = style.copy(fontSize = if (style.fontSize.isSpecified) style.fontSize * 1.25f else 26.sp), modifier = Modifier.clearAndSetSemantics {})
            }
        }
    }
}

/**
 * Selected-state badge: a white check on a disc in the hue's lip, so it never relies on hue alone. Lips are at
 * least 3:1 against paper and tints in every mode (ink in high contrast), so the badge stays visible on any face.
 */
@Composable
fun CheckBadge(hue: Hue, modifier: Modifier = Modifier) {
    Canvas(modifier.size(32.dp).clearAndSetSemantics {}) {
        drawCircle(hue.lip)
        val tick = Path().apply {
            moveTo(size.width * .28f, size.height * .53f)
            lineTo(size.width * .44f, size.height * .68f)
            lineTo(size.width * .74f, size.height * .34f)
        }
        drawPath(tick, Color.White, style = Stroke(3.5.dp.toPx(), cap = StrokeCap.Round))
    }
}

/** Soft ground shadow, tinted toward the character's hue rather than grey. */
private fun DrawScope.groundShadow(color: Color, center: Offset, width: Float) {
    scale(1f, .22f, center) { drawCircle(Brush.radialGradient(listOf(color.copy(alpha = .38f), color.copy(alpha = 0f)), center, width / 2), width / 2, center) }
}

/**
 * Original vector artwork, painted at any density; no external illustrations or emoji.
 * Characters are flat fills with a soft hue-tinted shadow and no ink outline. Coordinates are
 * content coordinates in a 300×300 box, independent of the RTL layout.
 */
@Composable
fun NuhaIllustration(kind: String, modifier: Modifier = Modifier, description: String? = null) {
    val c = LocalIdentity.current
    val reducedMotion = LocalReducedMotion.current
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val reveal by animateFloatAsState(
        targetValue = if (entered || reducedMotion) 1f else 0f,
        animationSpec = tween(if (reducedMotion) 0 else 240),
        label = "Illustration entrance",
    )
    Canvas(modifier.graphicsLayer { alpha = reveal }.then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier)) {
        val s = min(size.width, size.height) / 300f
        val dx = (size.width - 300*s)/2
        val dy = (size.height - 300*s)/2
        with(drawContext.canvas) { save(); translate(dx, dy); scale(s, s) }
        fun line(a: Offset, b: Offset, color: Color, width: Float = 5f) = drawLine(color, a, b, width, StrokeCap.Round)
        fun eye(x: Float, y: Float, r: Float = 1f) { drawOval(Color.White, Offset(x-15*r,y-21*r), Size(30f*r,42f*r)); drawOval(c.ink, Offset(x-5*r,y-11*r),Size(13f*r,24f*r)); drawCircle(Color.White,3f*r,Offset(x+3*r,y-6*r)) }
        fun smile(x: Float, y: Float, width: Float = 34f) { drawArc(c.ink, 10f, 160f, false, Offset(x-width/2,y-8), Size(width,22f), style = Stroke(4f, cap = StrokeCap.Round)) }
        fun closedEye(x: Float, y: Float) { drawArc(c.ink, 20f, 140f, false, Offset(x-13,y-9), Size(26f,16f), style = Stroke(4.5f, cap = StrokeCap.Round)) }
        fun body(path: Path, h: Hue, top: Float, bottom: Float) = drawPath(path, Brush.verticalGradient(listOf(h.fill.lifted(c.lift), h.fill), top, bottom))
        when (kind) {
            // Activity mascots (the lead guide is NuhaGuide): one shape character per activity, in that activity's hue.
            "mascot-matching" -> {
                val h = c.matching
                groundShadow(h.lip, 150f toO 262f, 210f)
                val squircle = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(52f, 58f, 248f, 248f, CornerRadius(62f))) }
                body(squircle, h, 58f, 248f)
                eye(122f,140f); eye(178f,140f); smile(150f,190f,48f)
            }
            "mascot-letters" -> {
                val h = c.letters
                groundShadow(h.lip, 150f toO 262f, 210f)
                val dome = Path().apply { moveTo(58f,250f); lineTo(58f,150f); cubicTo(58f,40f,242f,40f,242f,150f); lineTo(242f,250f); close() }
                body(dome, h, 50f, 250f)
                eye(124f,138f); eye(176f,138f)
                drawArc(c.ink, 20f, 140f, false, Offset(132f,170f), Size(36f,24f), style = Stroke(4.5f, cap = StrokeCap.Round))
            }
            "mascot-emotions" -> {
                val h = c.emotions
                groundShadow(h.lip, 150f toO 262f, 210f)
                val clover = Path().apply {
                    for ((x, y) in listOf(108f to 108f, 192f to 108f, 108f to 192f, 192f to 192f)) addOval(androidx.compose.ui.geometry.Rect(Offset(x, y), 58f))
                    addRect(androidx.compose.ui.geometry.Rect(100f, 100f, 200f, 200f))
                }
                body(clover, h, 50f, 250f)
                closedEye(126f,142f); closedEye(174f,142f); smile(150f,176f,52f)
            }
            "leaf" -> {
                val h = c.downy
                val p=Path().apply {moveTo(150f,256f);cubicTo(32f,197f,63f,76f,238f,42f);cubicTo(265f,166f,215f,232f,150f,256f);close()}
                body(p, h, 42f, 256f); line(140f toO 265f,220f toO 77f,h.lip,6f); line(180f toO 172f,130f toO 139f,h.lip,5f)
            }
        }
        drawContext.canvas.restore()
    }
}

/**
 * The lead mascot: a five-petal flower with a face and no limbs (ui-style.md, Mascot system), drawn in a
 * 300×300 content box. [gaze] (-1..1 per axis) moves the pupils so the guide looks at what it is pointing to.
 * It pops in on entry, and a tap hops it off its shadow and calls [onTap]; under reduced motion it appears
 * in place and a tap only calls [onTap].
 */
@Composable
fun NuhaGuide(modifier: Modifier = Modifier, gaze: Offset = Offset.Zero, description: String? = null, onTap: (() -> Unit)? = null) {
    val c = LocalIdentity.current
    val reducedMotion = LocalReducedMotion.current
    val appear = remember { Animatable(if (reducedMotion) 1f else 0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, spring(dampingRatio = .55f, stiffness = Spring.StiffnessLow)) }
    val hop = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val tap by rememberUpdatedState(onTap)
    val tappable = if (onTap == null) Modifier else Modifier
        .semantics { role = Role.Button; onClick { tap?.invoke(); true } }
        .pointerInput(reducedMotion) {
            detectTapGestures(onPress = {
                if (!reducedMotion && !hop.isRunning) scope.launch {
                    hop.animateTo(1f, tween(140))
                    hop.animateTo(0f, spring(dampingRatio = .45f, stiffness = Spring.StiffnessMediumLow))
                }
                tap?.invoke()
            })
        }
    Canvas(
        modifier
            .graphicsLayer {
                alpha = (appear.value * 2f).coerceIn(0f, 1f)
                val pop = .6f + .4f * appear.value
                scaleX = pop; scaleY = pop
                transformOrigin = TransformOrigin(.5f, .95f)
            }
            .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier)
            .then(tappable),
    ) {
        val s = min(size.width, size.height) / 300f
        with(drawContext.canvas) { save(); translate((size.width - 300*s)/2, (size.height - 300*s)/2); scale(s, s) }
        val h = c.guide
        groundShadow(h.lip, 150f toO 290f, 210f * (1f - hop.value * .3f))
        translate(top = -hop.value * 28.dp.toPx() / s) {
            val flower = Path().apply {
                for ((x, y, r) in listOf(Triple(150f, 78f, 64f), Triple(226f, 134f, 64f), Triple(198f, 222f, 64f), Triple(102f, 222f, 64f), Triple(74f, 134f, 64f), Triple(150f, 158f, 80f)))
                    addOval(androidx.compose.ui.geometry.Rect(Offset(x, y), r))
            }
            drawPath(flower, Brush.verticalGradient(listOf(h.fill.lifted(c.lift), h.fill), 14f, 286f))
            // A soft shine on the top petal, the one highlight soft-flat allows.
            rotate(-24f, 128f toO 50f) { drawOval(Color.White.copy(alpha = if (c.highContrast) 0f else .32f), Offset(110f, 42f), Size(38f, 17f)) }
            // Face: large and set low in the centre so the flower reads as a character first.
            val look = Offset(gaze.x.coerceIn(-1f, 1f) * 10f, gaze.y.coerceIn(-1f, 1f) * 10f + 2f)
            for (x in listOf(120f, 180f)) {
                drawOval(Color.White, Offset(x - 29f, 118f), Size(58f, 66f))
                drawCircle(c.ink, 18f, Offset(x, 151f) + look)
                drawCircle(Color.White, 5f, Offset(x + 5f, 144f) + look)
            }
            // Three lashes per eye, fanned to the outer side so the two sets never meet.
            for (side in listOf(-1f, 1f)) for ((a, b) in listOf((97f toO 130f) to (85f toO 119f), (106f toO 122f) to (99f toO 108f), (118f toO 118f) to (115f toO 103f))) {
                fun m(o: Offset) = if (side < 0) o else Offset(300f - o.x, o.y)
                drawLine(c.ink, m(a), m(b), 5.5f, StrokeCap.Round)
            }
            drawPath(Path().apply { moveTo(136f, 203f); quadraticTo(150f, 217f, 164f, 203f) }, c.ink, style = Stroke(6.5f, cap = StrokeCap.Round))
        }
        drawContext.canvas.restore()
    }
}

/** The shape buddies a child can pick as their avatar, in the order shown. Shapes avoid questions of dress and gender. */
val AvatarKinds = listOf("squircle", "dome", "clover", "sun", "bean", "triangle")

/** Each avatar keeps one hue everywhere it appears. */
fun IdentityPalette.avatarHue(kind: String): Hue = when (kind) {
    "squircle" -> pastelGreen
    "dome" -> summerSky
    "clover" -> illusion
    "sun" -> texasRose
    "bean" -> slateBlue
    else -> amethyst
}

/**
 * A shape buddy: a flat body in its hue with the guide's eyes and smile and no limbs, in a 100×100 content box.
 * [gaze] (-1..1 per axis) moves the pupils.
 */
@Composable
fun NuhaAvatar(kind: String, modifier: Modifier = Modifier, gaze: Offset = Offset.Zero) {
    val c = LocalIdentity.current
    val h = c.avatarHue(kind)
    Canvas(modifier.clearAndSetSemantics {}) {
        val s = min(size.width, size.height) / 100f
        with(drawContext.canvas) { save(); translate((size.width - 100*s)/2, (size.height - 100*s)/2); scale(s, s) }
        groundShadow(h.lip, 50f toO 94f, 64f)
        val brush = Brush.verticalGradient(listOf(h.fill.lifted(c.lift), h.fill), 10f, 92f)
        var faceY = 50f
        when (kind) {
            "squircle" -> drawRoundRect(brush, Offset(16f, 18f), Size(68f, 70f), CornerRadius(26f))
            "dome" -> { drawPath(Path().apply { moveTo(18f, 90f); lineTo(18f, 50f); cubicTo(18f, 6f, 82f, 6f, 82f, 50f); lineTo(82f, 90f); close() }, brush); faceY = 52f }
            "clover" -> drawPath(Path().apply {
                for ((x, y) in listOf(34f to 36f, 66f to 36f, 34f to 68f, 66f to 68f)) addOval(androidx.compose.ui.geometry.Rect(Offset(x, y), 20f))
                addOval(androidx.compose.ui.geometry.Rect(Offset(50f, 52f), 22f))
            }, brush)
            "sun" -> drawPath(Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(Offset(50f, 52f), 31f))
                for (i in 0 until 10) { val a = i * PI / 5; addOval(androidx.compose.ui.geometry.Rect(Offset(50f + (34 * cos(a)).toFloat(), 52f + (34 * sin(a)).toFloat()), 9f)) }
            }, brush)
            "bean" -> rotate(-10f, 50f toO 52f) { drawOval(brush, Offset(15f, 14f), Size(70f, 78f)) }
            else -> {
                // Rounded triangle: the fill plus a thick round-joined stroke of the same colour softens the corners.
                val tri = Path().apply { moveTo(50f, 20f); lineTo(84f, 82f); lineTo(16f, 82f); close() }
                drawPath(tri, brush); drawPath(tri, brush, style = Stroke(16f, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                faceY = 60f
            }
        }
        val look = Offset(gaze.x.coerceIn(-1f, 1f) * 3f, gaze.y.coerceIn(-1f, 1f) * 3f + .6f)
        for (x in listOf(40f, 60f)) {
            drawOval(Color.White, Offset(x - 8.5f, faceY - 10f), Size(17f, 19f))
            drawCircle(c.ink, 5.2f, Offset(x, faceY) + look)
            drawCircle(Color.White, 1.5f, Offset(x + 1.6f, faceY - 2f) + look)
        }
        drawPath(Path().apply { moveTo(45f, faceY + 13f); quadraticTo(50f, faceY + 18f, 55f, faceY + 13f) }, c.ink, style = Stroke(2.6f, cap = StrokeCap.Round))
        drawContext.canvas.restore()
    }
}

/**
 * Activity pictograms in a single colour, for drawing on a hue fill. Callers pass the hue's onFill,
 * which is at least 4.5:1 against the fill (3:1 for large-only hues), so the meaning survives every mode.
 */
@Composable
fun NuhaPictogram(kind: String, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.clearAndSetSemantics {}) {
        val s = min(size.width, size.height) / 100f
        with(drawContext.canvas) { save(); translate((size.width - 100*s)/2, (size.height - 100*s)/2); scale(s, s) }
        val stroke = Stroke(6f, cap = StrokeCap.Round)
        fun line(a: Offset, b: Offset, width: Float = 6f) = drawLine(color, a, b, width, StrokeCap.Round)
        when (kind) {
            // Matching: a pair of cards, one placed on its outline.
            "matching" -> {
                drawRoundRect(color, Offset(18f,30f), Size(34f,44f), CornerRadius(8f), style = stroke)
                drawRoundRect(color, Offset(52f,22f), Size(34f,44f), CornerRadius(8f))
                drawCircle(color, 7f, 35f toO 52f)
            }
            // Letters: an open book with ب.
            "letters" -> {
                val book = Path().apply { moveTo(14f,28f); quadraticTo(32f,20f,50f,30f); quadraticTo(68f,20f,86f,28f); lineTo(86f,78f); quadraticTo(68f,70f,50f,80f); quadraticTo(32f,70f,14f,78f); close() }
                drawPath(book, color, style = stroke); line(50f toO 30f, 50f toO 78f, 5f)
                drawArc(color, 0f, 180f, false, Offset(22f,40f), Size(22f,16f), style = Stroke(5f, cap = StrokeCap.Round)); drawCircle(color, 3.5f, 33f toO 64f)
                line(58f toO 44f, 76f toO 44f, 5f); line(58f toO 56f, 72f toO 56f, 5f)
            }
            // Attention and movement: a target with a steady centre.
            "focus" -> {
                drawCircle(color, 34f, 50f toO 50f, style = stroke)
                drawCircle(color, 19f, 50f toO 50f, style = stroke)
                drawCircle(color, 7f, 50f toO 50f)
            }
            // Emotions: a happy face and a small calm one.
            "emotions" -> {
                drawCircle(color, 28f, 42f toO 46f, style = stroke)
                drawCircle(color, 4f, 32f toO 40f); drawCircle(color, 4f, 52f toO 40f)
                drawArc(color, 20f, 140f, false, Offset(28f,44f), Size(28f,16f), style = Stroke(5f, cap = StrokeCap.Round))
                drawCircle(color, 14f, 76f toO 74f, style = Stroke(5f))
                line(70f toO 78f, 82f toO 78f, 4f)
            }
        }
        drawContext.canvas.restore()
    }
}
private infix fun Float.toO(y: Float) = Offset(this,y)
