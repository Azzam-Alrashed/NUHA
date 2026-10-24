package com.nuha.app.child

import android.os.SystemClock
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuha.app.audio.Effects
import com.nuha.app.audio.rememberEffects
import com.nuha.app.ui.identity.ChunkyButton
import com.nuha.app.ui.identity.ChunkySurface
import com.nuha.app.ui.identity.Hue
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.LocalReducedMotion
import com.nuha.app.ui.identity.NuhaAvatar
import com.nuha.app.ui.identity.NuhaGuide
import com.nuha.app.ui.identity.NuhaPictogram
import com.nuha.app.ui.identity.pressSink
import com.nuha.app.ui.theme.buttonWeight
import com.nuha.app.ui.theme.titleWeight

/**
 * The frame every activity runs in. Plain paper with only the task, its mascot and the frame's two controls
 * (FR-ACT-014): the map button on the right returns home in one action (FR-HOM-004), and the pause button on the
 * left is always visible and works for the child, with no gate (FR-ACT-009, FR-ADM-004). Pausing covers the task
 * with a rest card offering «نكمل» and «انتهيت». [content] receives whether play is paused, so activities can stop
 * their clocks.
 */
@Composable
fun ActivityFrame(activity: Activity, onMap: () -> Unit, effects: Boolean, content: @Composable BoxScope.(paused: Boolean) -> Unit) {
    val c = LocalIdentity.current
    val sounds = rememberEffects(effects)
    val hue = c.activityHue(activity)
    var paused by rememberSaveable { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(c.paper)) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 28.dp, vertical = 16.dp)) {
            // RTL: the first child sits on the right.
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                RoundIconButton("home", ChildCopy.Map, c.action, sounds, onMap)
                Spacer(Modifier.width(20.dp))
                ActivityBadge(activity, hue)
                Spacer(Modifier.weight(1f))
                RoundIconButton("pause", ChildCopy.Pause, c.action, sounds) { paused = true }
            }
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { content(paused) }
        }
        if (paused) PauseCard(sounds, onResume = { paused = false }, onStop = onMap)
    }
}

/** The activity's hue tile, pictogram and short name, so the child sees where they are. */
@Composable
private fun ActivityBadge(activity: Activity, hue: Hue) {
    val c = LocalIdentity.current
    Row(Modifier.semantics(mergeDescendants = true) { heading() }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.size(64.dp).background(hue.fill, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
            NuhaPictogram(activity.pictogram, hue.onFill, Modifier.size(40.dp))
        }
        Text(activity.label, color = c.ink, style = MaterialTheme.typography.headlineMedium.titleWeight().copy(fontSize = 34.sp))
    }
}

/** Round chunky control with a pictogram on the hue's tint; fires on touch-down like ChunkyButton. */
@Composable
fun RoundIconButton(icon: String, label: String, hue: Hue, sounds: Effects?, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    var lastFired by remember { mutableLongStateOf(0L) }
    val action by rememberUpdatedState(onClick)
    val fire = { sounds?.pop(); action() }
    ChunkySurface(
        hue, hue.tint,
        Modifier
            .size(88.dp)
            .semantics(mergeDescendants = true) { role = Role.Button; contentDescription = label; onClick { fire(); true } }
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    pressed = true
                    val now = SystemClock.uptimeMillis()
                    if (now - lastFired >= 300) { lastFired = now; fire() }
                    tryAwaitRelease()
                    pressed = false
                })
            },
        lip = 5.dp, sink = pressSink(pressed, 5.dp), corner = null,
    ) { NuhaPictogram(icon, hue.lip, Modifier.size(42.dp)) }
}

/** The rest card: the guide resting, «استراحة», and the two ways on. Taps outside the card do nothing. */
@Composable
private fun PauseCard(sounds: Effects?, onResume: () -> Unit, onStop: () -> Unit) {
    val c = LocalIdentity.current
    val reduced = LocalReducedMotion.current
    var shown by remember { mutableStateOf(reduced) }
    androidx.compose.runtime.LaunchedEffect(Unit) { shown = true }
    val fade by animateFloatAsState(if (shown) 1f else 0f, tween(if (reduced) 0 else 180), label = "Pause card")
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = fade }
            .background(c.ink.copy(alpha = .55f))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .widthIn(min = 620.dp)
                .drawBehind {
                    val lip = 8.dp.toPx()
                    val face = Size(size.width, size.height - lip)
                    val r = CornerRadius(40.dp.toPx())
                    drawRoundRect(c.scene.tileLip, Offset(0f, lip), face, r)
                    drawRoundRect(Color.White, Offset.Zero, face, r)
                }
                .padding(start = 48.dp, end = 48.dp, top = 28.dp, bottom = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            NuhaGuide(Modifier.size(190.dp), gaze = Offset(0f, .8f))
            Text(ChildCopy.Pause, color = c.ink, style = MaterialTheme.typography.displaySmall.titleWeight().copy(fontSize = 52.sp, lineHeight = 76.sp), modifier = Modifier.semantics { heading() })
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                val style = MaterialTheme.typography.displaySmall.buttonWeight().copy(fontSize = 36.sp, lineHeight = 48.sp)
                ChunkyButton(ChildCopy.Resume, { sounds?.pop(); onResume() }, Modifier.height(104.dp).widthIn(min = 240.dp), style = style)
                ChunkyButton(ChildCopy.Done, { sounds?.pop(); onStop() }, Modifier.height(104.dp).widthIn(min = 240.dp), secondary = true, style = style)
            }
        }
    }
}

/** Stand-in body until the activity is built (milestones 2–3): its mascot and a note. */
@Composable
fun UnderConstruction(activity: Activity) {
    val c = LocalIdentity.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        NuhaAvatar(activity.mascot, Modifier.size(240.dp), hue = c.activityHue(activity))
        Text(ChildCopy.UnderConstruction, color = c.ink, style = MaterialTheme.typography.headlineMedium.titleWeight())
    }
}
