package com.nuha.app.child

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nuha.app.Numerals
import com.nuha.app.Pending
import com.nuha.app.activities.ActivityContent
import com.nuha.app.activities.planFor
import com.nuha.app.adult.AdultEntry
import com.nuha.app.audio.rememberEffects
import com.nuha.app.audio.rememberSpeech
import com.nuha.app.data.AppDatabase
import com.nuha.app.data.ChildProfile
import com.nuha.app.data.ChildSettings
import com.nuha.app.task.TaskScreen
import com.nuha.app.ui.identity.ChunkyButton
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.Motion
import com.nuha.app.ui.identity.NuhaGuide
import com.nuha.app.ui.scene.SpeechBubble
import com.nuha.app.ui.scene.drawSky
import com.nuha.app.ui.scene.placeAt
import com.nuha.app.ui.theme.NuhaTheme
import com.nuha.app.ui.theme.buttonWeight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The child who is playing: their profile, their settings (FR-SET-002) and their completed sessions. */
class ChildViewModel(application: Application, private val childId: Long) : AndroidViewModel(application) {
    private val database = AppDatabase.get(application)
    val child: StateFlow<ChildProfile?> = database.children().observeAll().map { all -> all.firstOrNull { it.id == childId } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    /** Read live, so a change made in the adult area shows the next time child mode is on screen. */
    val settings: StateFlow<ChildSettings?> = database.settings().observe(childId).map { it ?: ChildSettings(childId) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val completed = MutableStateFlow<Map<String, Int>>(emptyMap())
    /** Completed sessions per activity id, for the stars on the map. */
    val completedSessions = completed.asStateFlow()
    fun refresh() = viewModelScope.launch {
        completed.value = database.records().completedByActivity(childId).associate { it.activity to it.completed }
    }
}

private const val MAP = "map"
private const val GOODBYE = "goodbye"

/**
 * Child mode for one child, two levels deep (FR-HOM-004): the map, then one activity. The goodbye screen closes a
 * session. Everything inside follows the child's own settings: theme, motion, numerals, sound, celebration, mascot,
 * timing, target size and map lock. System Back mirrors the on-screen controls: activity → map, map → goodbye,
 * goodbye → map. The only way out to adult screens is the gate's entry on the goodbye screen (FR-ADM-001).
 * «نلعب مرة أخرى» calls [onAgain], which asks who plays next when there are several children, else returns null and
 * the same child goes back to the map.
 */
@Composable
fun ChildMode(childId: Long, onAgain: (() -> Unit)?, onAdult: () -> Unit) {
    val app = LocalContext.current.applicationContext as Application
    val model: ChildViewModel = viewModel(key = "child-$childId") { ChildViewModel(app, childId) }
    val child by model.child.collectAsStateWithLifecycle()
    val settings by model.settings.collectAsStateWithLifecycle()
    val c = LocalIdentity.current
    // Read before the loading return below, so a configuration change brings the child back to the same screen.
    var screen by rememberSaveable(childId) { mutableStateOf(MAP) }
    val current = child
    val s = settings
    if (current == null || s == null) {
        Box(Modifier.fillMaxSize().background(c.scene.ground))
        return
    }
    val numerals = Numerals.of(s.numerals)
    NuhaTheme(appearance = s.appearance, motion = Motion.of(s.motion), numerals = numerals) {
        CompositionLocalProvider(LocalChildSettings provides s) {
            val lock = s.mapLock?.let(Activity::byId)
            when (screen) {
                MAP -> {
                    BackHandler { screen = GOODBYE }
                    LaunchedEffect(Unit) { model.refresh() }
                    val completed by model.completedSessions.collectAsStateWithLifecycle()
                    HomeMap(
                        current, stars = { Pending.starsFor(completed[it.id] ?: 0) }, onOpen = { screen = it.id }, onDone = { screen = GOODBYE },
                        voice = s.voice, effects = s.effects, lock = lock,
                    )
                }
                GOODBYE -> {
                    BackHandler { screen = MAP }
                    GoodbyeScreen(current, onAgain = { if (onAgain != null) onAgain() else screen = MAP }, onAdult = onAdult, voice = s.voice, effects = s.effects)
                }
                else -> {
                    val activity = Activity.byId(screen) ?: Activity.Emotions
                    TaskScreen(activity, current, { planFor(activity, it, numerals) }, s.voice, s.effects, onMap = { screen = MAP }) { scope -> ActivityContent(activity, scope) }
                }
            }
        }
    }
}

/**
 * Closing ritual: the guide says goodbye by name; «نلعب مرة أخرى» goes on. The «للكبار» entry sits in the far corner,
 * where a grown-up takes over once the child is done.
 */
@Composable
private fun GoodbyeScreen(child: ChildProfile, onAgain: () -> Unit, onAdult: () -> Unit, voice: Boolean, effects: Boolean) {
    val c = LocalIdentity.current
    val speech = rememberSpeech(voice)
    val sounds = rememberEffects(effects)
    val line = ChildCopy.goodbye(child.name)
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(300); speech?.say(line) }
    BoxWithConstraints(Modifier.fillMaxSize().background(c.scene.ground), contentAlignment = AbsoluteAlignment.TopLeft) {
        val w = maxWidth
        val h = maxHeight
        Canvas(Modifier.fillMaxSize().clearAndSetSemantics {}) { drawSky(c.scene) }
        val guideX = w * .5f
        val guideY = h * .52f
        NuhaGuide(Modifier.placeAt(guideX, guideY).size(300.dp), gaze = Offset(0f, .2f), description = Pending.GuideName, onTap = { speech?.say(line) })
        SpeechBubble(line, Modifier.placeAt(guideX + 92.dp, guideY - 132.dp, alignX = 1f, alignY = 1f))
        AdultEntry(onAdult, Modifier.align(AbsoluteAlignment.TopLeft).statusBarsPadding().padding(horizontal = 24.dp, vertical = 12.dp))
        Box(Modifier.fillMaxSize().navigationBarsPadding().padding(bottom = 24.dp), contentAlignment = AbsoluteAlignment.BottomLeft) {
            ChunkyButton(
                ChildCopy.PlayAgain, { sounds?.pop(); onAgain() },
                Modifier.placeAt(w * (531f / 640f), 0.dp, alignY = 1f).height(108.dp).widthIn(min = 320.dp),
                style = MaterialTheme.typography.displaySmall.buttonWeight().copy(fontSize = 40.sp, lineHeight = 52.sp),
            )
        }
    }
}
