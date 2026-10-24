package com.nuha.app.task

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuha.app.Pending
import com.nuha.app.audio.rememberEffects
import com.nuha.app.child.ChildCopy
import com.nuha.app.data.ChildProfile
import com.nuha.app.ui.identity.ChunkyButton
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.LocalReducedMotion
import com.nuha.app.ui.identity.NuhaAvatar
import com.nuha.app.ui.identity.NuhaGuide
import com.nuha.app.ui.identity.starPath
import com.nuha.app.ui.scene.drawSky
import com.nuha.app.ui.scene.placeAt
import com.nuha.app.ui.theme.buttonWeight
import com.nuha.app.ui.theme.titleWeight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.random.Random

/**
 * Per-session celebration (FR-RWD-003): a short confetti burst, the ribbon «أحسنت!», the stars earned and the count.
 * Confetti is the one effect allowed past 1.5 s; under reduced motion, or with celebration off, the ribbon is static
 * and there is no confetti (FR-RWD-005, NFR-ACC-006). Then the child chooses: play again or go to the map.
 */
@Composable
fun SessionCelebration(child: ChildProfile, filled: Int, slots: Int, onAgain: () -> Unit, onMap: () -> Unit, effects: Boolean) {
    val c = LocalIdentity.current
    val reduced = LocalReducedMotion.current
    val sounds = rememberEffects(effects)
    val intensity = Pending.CelebrationIntensity
    BoxWithConstraints(Modifier.fillMaxSize().background(c.scene.ground), contentAlignment = AbsoluteAlignment.TopLeft) {
        val w = maxWidth
        Canvas(Modifier.fillMaxSize().clearAndSetSemantics {}) { drawSky(c.scene) }
        if (!reduced && intensity != "off") Confetti(lively = intensity == "lively")
        Column(
            Modifier.placeAt(w / 2, 70.dp, alignY = 0f).semantics(mergeDescendants = true) { contentDescription = "${ChildCopy.Bravo} ${ChildCopy.completed(filled, slots)}" },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Ribbon(ChildCopy.Bravo)
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(slots) { i ->
                    Canvas(Modifier.size(76.dp)) {
                        val path = starPath(center, size.minDimension / 2)
                        if (i < filled) { drawPath(path, c.texasRose.fill); drawPath(path, c.texasRose.lip, style = Stroke(3.dp.toPx(), join = StrokeJoin.Round)) }
                        else drawPath(path, c.scene.tileLip, style = Stroke(3.dp.toPx(), join = StrokeJoin.Round))
                    }
                }
            }
            Text(ChildCopy.completed(filled, slots), color = c.ink, style = MaterialTheme.typography.headlineMedium.titleWeight().copy(fontSize = 34.sp))
        }
        NuhaGuide(Modifier.placeAt(w * .2f, 560.dp).size(220.dp), gaze = Offset(.6f, -.4f))
        NuhaAvatar(child.avatar, Modifier.placeAt(w * .2f + 150.dp, 600.dp).size(110.dp), gaze = Offset(-.8f, -.3f))
        Box(Modifier.fillMaxSize().navigationBarsPadding().padding(bottom = 24.dp), contentAlignment = AbsoluteAlignment.BottomLeft) {
            ChunkyButton(
                ChildCopy.PlayAgain, { sounds?.pop(); onAgain() },
                Modifier.placeAt(w * (531f / 640f), 0.dp, alignY = 1f).height(108.dp).widthIn(min = 320.dp),
                style = MaterialTheme.typography.displaySmall.buttonWeight().copy(fontSize = 40.sp, lineHeight = 52.sp),
            )
            ChunkyButton(
                ChildCopy.Map, { sounds?.pop(); onMap() },
                Modifier.placeAt(w * .5f, 0.dp, alignY = 1f).height(96.dp).widthIn(min = 220.dp), secondary = true,
                style = MaterialTheme.typography.displaySmall.buttonWeight().copy(fontSize = 32.sp, lineHeight = 44.sp),
            )
        }
    }
}

/** A banner in the action hue with folded ends in its lip colour. */
@Composable
private fun Ribbon(text: String) {
    val c = LocalIdentity.current
    val h = c.action
    Box(
        Modifier
            .drawBehind {
                val tail = 44.dp.toPx(); val drop = 18.dp.toPx(); val notch = 20.dp.toPx()
                for (side in listOf(-1f, 1f)) {
                    val x0 = if (side < 0) 0f else size.width
                    drawPath(Path().apply {
                        moveTo(x0, drop); lineTo(x0 + side * tail, drop); lineTo(x0 + side * (tail - notch), drop + (size.height) / 2); lineTo(x0 + side * tail, size.height + drop); lineTo(x0, size.height + drop); close()
                    }, h.lip)
                }
                drawRoundRect(h.fill, Offset.Zero, Size(size.width, size.height), androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()))
            }
            .padding(horizontal = 64.dp, vertical = 10.dp)
            .semantics { heading() },
    ) {
        Text(text, color = h.onFill, style = MaterialTheme.typography.displaySmall.titleWeight().copy(fontSize = 64.sp, lineHeight = 90.sp))
    }
}

/** One burst of flat confetti from the top, about 1.8 s (gentle) or 2.6 s (lively), in the palette's hues. */
@Composable
private fun Confetti(lively: Boolean) {
    val c = LocalIdentity.current
    val count = if (lively) 140 else 70
    val duration = if (lively) 2_600 else 1_800
    val colors = listOf(c.amethyst.fill, c.summerSky.fill, c.pastelGreen.fill, c.texasRose.fill, c.monaLisa.fill, c.illusion.fill, c.downy.fill)
    val bits = remember { val r = Random(7); List(count) { floatArrayOf(r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat()) } }
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(duration, easing = LinearEasing)) }
    if (t.value >= 1f) return
    Canvas(Modifier.fillMaxSize().clearAndSetSemantics {}) {
        val p = t.value
        bits.forEachIndexed { i, (a, b, s, k) ->
            val x = size.width * (.15f + .7f * a) + cos((a * 6 + p * 8) * PI.toFloat()) * 30f
            val y = -40f + (size.height * .9f) * (p * (0.7f + .5f * b)) + 400f * p * p * s
            val fade = if (p > .75f) (1f - p) / .25f else 1f
            rotate(360f * (k + p * (1 + s)), Offset(x, y)) {
                drawRect(colors[i % colors.size].copy(alpha = fade), Offset(x - 9f, y - 5f), Size(18f, 10f))
            }
        }
    }
}

