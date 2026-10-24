package com.nuha.app.child

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuha.app.Pending
import com.nuha.app.adult.AdultEntry
import com.nuha.app.audio.rememberEffects
import com.nuha.app.audio.rememberSpeech
import com.nuha.app.data.ChildProfile
import com.nuha.app.onboarding.ChoiceTile
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.NuhaAvatar
import com.nuha.app.ui.identity.NuhaGuide
import com.nuha.app.ui.identity.avatarHue
import com.nuha.app.ui.scene.SpeechBubble
import com.nuha.app.ui.scene.drawSky
import com.nuha.app.ui.scene.placeAt
import com.nuha.app.ui.theme.titleWeight

/**
 * «من يلعب الآن؟» (FR-PRF-008): with several children on the device, each one's buddy and name, and one tap opens
 * that child's map. The «للكبار» entry waits in the far corner for a grown-up.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChildPicker(children: List<ChildProfile>, onPick: (ChildProfile) -> Unit, onAdult: () -> Unit, voice: Boolean, effects: Boolean) {
    val c = LocalIdentity.current
    val speech = rememberSpeech(voice)
    val sounds = rememberEffects(effects)
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(300); speech?.say(ChildCopy.WhoPlays) }
    BoxWithConstraints(Modifier.fillMaxSize().background(c.scene.ground), contentAlignment = AbsoluteAlignment.TopLeft) {
        val w = maxWidth
        val h = maxHeight
        Canvas(Modifier.fillMaxSize().clearAndSetSemantics {}) { drawSky(c.scene) }
        NuhaGuide(Modifier.placeAt(w - 150.dp, h - 150.dp).size(220.dp), gaze = Offset(-1f, -.3f), description = Pending.GuideName, onTap = { speech?.say(ChildCopy.WhoPlays) })
        SpeechBubble(ChildCopy.WhoPlays, Modifier.placeAt(w - 230.dp, h - 290.dp, alignX = 1f, alignY = 1f), fontSize = 40.sp)
        Column(
            Modifier.fillMaxWidth().padding(top = h * .2f, start = 48.dp, end = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(32.dp)) {
                for (child in children) ChoiceTile(
                    c.avatarHue(child.avatar), selected = false, onSelect = { sounds?.pop(); onPick(child) },
                    description = child.name, modifier = Modifier.size(width = 220.dp, height = 240.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        NuhaAvatar(child.avatar, Modifier.size(130.dp))
                        Text(child.name, color = c.ink, style = MaterialTheme.typography.headlineMedium.titleWeight().copy(fontSize = 30.sp))
                    }
                }
            }
        }
        Box(Modifier.align(AbsoluteAlignment.TopLeft).statusBarsPadding().padding(horizontal = 24.dp, vertical = 12.dp)) { AdultEntry(onAdult) }
    }
}
