package com.nuha.app.child

import android.os.SystemClock
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuha.app.Pending
import com.nuha.app.audio.Effects
import com.nuha.app.audio.rememberEffects
import com.nuha.app.audio.rememberSpeech
import com.nuha.app.data.ChildProfile
import com.nuha.app.ui.identity.ChunkyButton
import com.nuha.app.ui.identity.ChunkySurface
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.LocalReducedMotion
import com.nuha.app.ui.identity.NuhaAvatar
import com.nuha.app.ui.identity.NuhaGuide
import com.nuha.app.ui.identity.NuhaPictogram
import com.nuha.app.ui.identity.pressSink
import com.nuha.app.ui.identity.starPath
import com.nuha.app.ui.scene.bloom
import com.nuha.app.ui.scene.bush
import com.nuha.app.ui.scene.drawSky
import com.nuha.app.ui.scene.placeAt
import com.nuha.app.ui.scene.smoothPath
import com.nuha.app.ui.theme.buttonWeight
import com.nuha.app.ui.theme.titleWeight
import kotlin.math.hypot

/** Each stop's centre in 640×400 sketch coordinates. Fixed: stops never move between sessions (FR-HOM-002). */
private val StopAt = mapOf(
    Activity.Emotions to Offset(500f, 318f), Activity.Environment to Offset(330f, 322f), Activity.SocialPath to Offset(160f, 318f),
    Activity.Star to Offset(160f, 200f), Activity.Maze to Offset(330f, 204f), Activity.Garden to Offset(500f, 200f),
    Activity.Letters to Offset(500f, 88f), Activity.Patterns to Offset(330f, 92f), Activity.Balance to Offset(160f, 88f),
)

/** The road, entering at the bottom right and leaving at the top left, so it reads in RTL order (FR-HOM-001). */
private val RoadThrough = listOf(
    Offset(700f, 334f), Offset(600f, 320f), Offset(500f, 318f), Offset(330f, 322f), Offset(160f, 318f), Offset(82f, 296f),
    Offset(72f, 226f), Offset(160f, 200f), Offset(330f, 204f), Offset(500f, 200f), Offset(574f, 180f), Offset(580f, 110f),
    Offset(500f, 88f), Offset(330f, 92f), Offset(160f, 88f), Offset(60f, 78f), Offset(-40f, 72f),
)

private const val STOP = 112
private const val CURRENT_STOP = 132
/** Every stop sits in the same square, so the circle's centre lands on its road point whatever its size. */
private const val STOP_BOX = CURRENT_STOP + 24
private const val MAX_STARS = 3

/**
 * Child home: the saga map (FR-HOM). Nine stops along a winding road, each in its activity's hue with its pictogram,
 * mascot and up to three stars. The stop for the child's starting path is larger, ringed, and has the child's buddy
 * beside it (FR-HOM-005). One tap opens an activity (FR-HOM-004); «انتهيت» ends the session with no gate (FR-ADM-004).
 * All nine stops fit without scrolling (FR-HOM-003). Scenery lives here and on celebration screens only (FR-HOM-008).
 */
@Composable
fun HomeMap(child: ChildProfile, stars: (Activity) -> Int, onOpen: (Activity) -> Unit, onDone: () -> Unit, voice: Boolean, effects: Boolean) {
    val c = LocalIdentity.current
    val speech = rememberSpeech(voice)
    val sounds = rememberEffects(effects)
    val current = Pending.startStop(child.startPath)
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(400); speech?.say(ChildCopy.mapGreeting(child.name)) }

    BoxWithConstraints(Modifier.fillMaxSize().background(c.scene.ground), contentAlignment = AbsoluteAlignment.TopLeft) {
        val w = maxWidth
        val h = maxHeight
        fun x(f: Float) = w * (f / 640f)
        fun y(f: Float) = h * (f / 400f)
        Canvas(Modifier.fillMaxSize().clearAndSetSemantics {}) {
            fun p(o: Offset) = Offset(size.width * o.x / 640f, size.height * o.y / 400f)
            val u = size.width / 640f
            drawSky(c.scene)
            val road = smoothPath(RoadThrough.map(::p))
            drawPath(road, c.scene.road, style = Stroke(46f * u, cap = StrokeCap.Round))
            drawPath(road, c.scene.dash, style = Stroke(4.5f * u, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(13f * u, 19f * u))))
            // Roadside details in the gaps between the road's rows and labels.
            bush(c.pastelGreen, p(Offset(245f, 266f)), 13f * u)
            bush(c.pastelGreen, p(Offset(418f, 268f)), 11f * u)
            bloom(c.illusion, c.texasRose.fill, p(Offset(22f, 150f)), 5f * u)
            bloom(c.monaLisa, c.texasRose.fill, p(Offset(616f, 262f)), 5f * u)
            bloom(c.illusion, c.texasRose.fill, p(Offset(248f, 150f)), 4f * u)
            bloom(c.monaLisa, c.texasRose.fill, p(Offset(415f, 150f)), 4f * u)
        }

        for (activity in Activity.entries) {
            val at = StopAt.getValue(activity)
            MapStop(
                activity, activity == current, stars(activity), sounds, onOpen,
                Modifier.placeAt(x(at.x), y(at.y) - (STOP_BOX / 2).dp, alignY = 0f),
            )
        }
        // The child's buddy waits on the road just past the starting stop, facing the way the road goes (leftward in RTL).
        val start = StopAt.getValue(current)
        NuhaAvatar(child.avatar, Modifier.placeAt(x(start.x) - (CURRENT_STOP / 2 + 46).dp, y(start.y) + 4.dp).size(76.dp), gaze = Offset(-1f, 0f))

        // The guide stands where the road comes in and looks toward the starting stop.
        val guideAt = Offset(w.value - 76f, h.value - 74f)
        val look = Offset(x(start.x).value - guideAt.x, y(start.y).value - guideAt.y).let { it / hypot(it.x, it.y) }
        NuhaGuide(Modifier.placeAt(guideAt.x.dp, guideAt.y.dp).size(170.dp), gaze = look, description = Pending.GuideName, onTap = { speech?.say(ChildCopy.MapHint) })

        Box(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp, vertical = 12.dp)) {
            ChildChip(child, Modifier.align(AbsoluteAlignment.TopRight))
            ChunkyButton(
                ChildCopy.Done, { sounds?.pop(); onDone() }, Modifier.align(AbsoluteAlignment.TopLeft).height(80.dp),
                secondary = true, style = MaterialTheme.typography.titleLarge.buttonWeight().copy(fontSize = 28.sp),
            )
        }
    }
}

@Composable
private fun MapStop(activity: Activity, current: Boolean, earned: Int, sounds: Effects?, onOpen: (Activity) -> Unit, modifier: Modifier) {
    val c = LocalIdentity.current
    val reduced = LocalReducedMotion.current
    val hue = c.activityHue(activity)
    val size = (if (current) CURRENT_STOP else STOP).dp
    var pressed by remember { mutableStateOf(false) }
    var lastFired by remember { mutableLongStateOf(0L) }
    val open by rememberUpdatedState(onOpen)
    val fire = { sounds?.pop(); open(activity) }
    val sink = pressSink(pressed, 6.dp)
    val pulse = if (current && !reduced) {
        rememberInfiniteTransition(label = "Current stop").animateFloat(0f, 1f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "Ring").value
    } else 0f
    val description = buildString {
        append(activity.label); append("، "); append(ChildCopy.stars(earned.coerceIn(0, MAX_STARS)))
        if (current) { append("، "); append(ChildCopy.StartHere) }
    }
    Column(
        modifier
            .width(200.dp)
            .semantics(mergeDescendants = true) { role = Role.Button; contentDescription = description; onClick { fire(); true } }
            .pointerInput(activity) {
                detectTapGestures(onPress = {
                    pressed = true
                    val now = SystemClock.uptimeMillis()
                    if (now - lastFired >= 300) { lastFired = now; fire() }
                    tryAwaitRelease()
                    pressed = false
                })
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(STOP_BOX.dp), contentAlignment = Alignment.Center) {
            if (current) Canvas(Modifier.size(size + 22.dp).graphicsLayer { scaleX = 1f + .06f * pulse; scaleY = 1f + .06f * pulse }) {
                drawCircle(hue.tint.copy(alpha = .9f - .4f * pulse))
                // The lip-coloured edge keeps the ring visible where the tint is white (high contrast).
                drawCircle(hue.lip, style = Stroke(3.dp.toPx()), radius = this.size.minDimension / 2 - 1.5.dp.toPx())
            }
            // A white ring separates the stop from the road and from its mascot.
            Box(Modifier.size(size + 10.dp).background(Color.White, CircleShape))
            ChunkySurface(hue, hue.fill, Modifier.size(size), lip = 6.dp, sink = sink, corner = null) {
                NuhaPictogram(activity.pictogram, hue.onFill, Modifier.size(size * .56f))
            }
            NuhaAvatar(activity.mascot, Modifier.align(AbsoluteAlignment.TopRight).offset(x = (-2).dp, y = 2.dp).size(if (current) 56.dp else 48.dp), hue = hue)
            StarPill(earned, Modifier.align(Alignment.TopCenter).offset(y = (STOP_BOX / 2).dp + size / 2 - 14.dp))
        }
        Text(
            activity.label, color = c.ink, textAlign = TextAlign.Center, maxLines = 1,
            style = MaterialTheme.typography.titleLarge.titleWeight().copy(fontSize = if (current) 28.sp else 24.sp),
        )
    }
}

/** Up to three stars earned on this activity; empty slots stay visible so progress is easy to read. */
@Composable
private fun StarPill(earned: Int, modifier: Modifier) {
    val c = LocalIdentity.current
    Row(
        modifier.background(Color.White, CircleShape).padding(horizontal = 8.dp, vertical = 4.dp).clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        repeat(MAX_STARS) { i ->
            Canvas(Modifier.size(20.dp)) {
                val path = starPath(center, size.minDimension / 2)
                // Earned stars are solid; empty slots are outlines, so the two differ by shape as well as colour.
                if (i < earned) {
                    drawPath(path, c.texasRose.fill)
                    drawPath(path, c.texasRose.lip, style = Stroke(1.5.dp.toPx()))
                } else {
                    if (!c.highContrast) drawPath(path, c.scene.tileLip.copy(alpha = .45f))
                    drawPath(path, c.scene.tileLip, style = Stroke(2.dp.toPx(), join = androidx.compose.ui.graphics.StrokeJoin.Round))
                }
            }
        }
    }
}

/** The child's buddy and name in a white pill, so the map is visibly theirs (FR-HOM-007). */
@Composable
private fun ChildChip(child: ChildProfile, modifier: Modifier) {
    val c = LocalIdentity.current
    Row(
        modifier.background(Color.White, CircleShape).padding(start = 22.dp, end = 10.dp, top = 6.dp, bottom = 6.dp).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        NuhaAvatar(child.avatar, Modifier.size(56.dp))
        Text(child.name, color = c.ink, style = MaterialTheme.typography.titleLarge.titleWeight().copy(fontSize = 28.sp), maxLines = 1)
    }
}

