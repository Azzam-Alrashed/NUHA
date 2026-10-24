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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nuha.app.Pending
import com.nuha.app.activities.ActivityContent
import com.nuha.app.activities.planFor
import com.nuha.app.audio.rememberEffects
import com.nuha.app.audio.rememberSpeech
import com.nuha.app.data.AppDatabase
import com.nuha.app.data.ChildProfile
import com.nuha.app.task.TaskScreen
import com.nuha.app.ui.identity.ChunkyButton
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.NuhaGuide
import com.nuha.app.ui.scene.SpeechBubble
import com.nuha.app.ui.scene.drawSky
import com.nuha.app.ui.scene.placeAt
import com.nuha.app.ui.theme.buttonWeight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** The child who is playing. Until several profiles are supported (FR-PRF-008, milestone 4) that is the first one. */
class ChildViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.get(application)
    private val mutable = MutableStateFlow<ChildProfile?>(null)
    val child = mutable.asStateFlow()
    private val completed = MutableStateFlow<Map<String, Int>>(emptyMap())
    /** Completed sessions per activity id, for the stars on the map. */
    val completedSessions = completed.asStateFlow()
    init { viewModelScope.launch { mutable.value = database.children().first(); refresh() } }
    fun refresh() = viewModelScope.launch {
        val id = mutable.value?.id ?: return@launch
        completed.value = database.records().completedByActivity(id).associate { it.activity to it.completed }
    }
}

private const val MAP = "map"
private const val GOODBYE = "goodbye"

/**
 * Child mode, two levels deep (FR-HOM-004): the map, then one activity. The goodbye screen closes a session.
 * System Back mirrors the on-screen controls: activity → map, map → goodbye, goodbye → map. Nothing in child mode
 * leads to adult screens (FR-ADM-001).
 */
@Composable
fun ChildMode(voice: Boolean, effects: Boolean) {
    val model: ChildViewModel = viewModel()
    val child by model.child.collectAsStateWithLifecycle()
    val c = LocalIdentity.current
    val current = child
    if (current == null) {
        Box(Modifier.fillMaxSize().background(c.scene.ground))
        return
    }
    var screen by rememberSaveable { mutableStateOf(MAP) }
    when (screen) {
        MAP -> {
            BackHandler { screen = GOODBYE }
            LaunchedEffect(Unit) { model.refresh() }
            val completed by model.completedSessions.collectAsStateWithLifecycle()
            HomeMap(current, stars = { Pending.starsFor(completed[it.id] ?: 0) }, onOpen = { screen = it.id }, onDone = { screen = GOODBYE }, voice = voice, effects = effects)
        }
        GOODBYE -> {
            BackHandler { screen = MAP }
            GoodbyeScreen(current, onAgain = { screen = MAP }, voice = voice, effects = effects)
        }
        else -> {
            val activity = Activity.byId(screen) ?: Activity.Emotions
            TaskScreen(activity, current, { planFor(activity, it) }, voice, effects, onMap = { screen = MAP }) { scope -> ActivityContent(activity, scope) }
        }
    }
}

/** Closing ritual: the guide says goodbye by name; «نلعب مرة أخرى» returns to the map. */
@Composable
private fun GoodbyeScreen(child: ChildProfile, onAgain: () -> Unit, voice: Boolean, effects: Boolean) {
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
        Box(Modifier.fillMaxSize().navigationBarsPadding().padding(bottom = 24.dp), contentAlignment = AbsoluteAlignment.BottomLeft) {
            ChunkyButton(
                ChildCopy.PlayAgain, { sounds?.pop(); onAgain() },
                Modifier.placeAt(w * (531f / 640f), 0.dp, alignY = 1f).height(108.dp).widthIn(min = 320.dp),
                style = MaterialTheme.typography.displaySmall.buttonWeight().copy(fontSize = 40.sp, lineHeight = 52.sp),
            )
        }
    }
}
