package com.nuha.app

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nuha.app.adult.AdultHome
import com.nuha.app.adult.GateScreen
import com.nuha.app.adult.ProfileScreen
import com.nuha.app.adult.ProfilesViewModel
import com.nuha.app.adult.SensoryScreen
import com.nuha.app.adult.SettingsScreen
import com.nuha.app.child.ChildMode
import com.nuha.app.child.ChildPicker
import com.nuha.app.data.AppPreferences
import com.nuha.app.data.PreferencesViewModel
import com.nuha.app.onboarding.OnboardingViewModel
import com.nuha.app.onboarding.QuestionsScreen
import com.nuha.app.onboarding.WelcomeScreen
import com.nuha.app.onboarding.WhoScreen
import com.nuha.app.ui.identity.Motion
import com.nuha.app.ui.theme.NuhaTheme
import com.nuha.app.ui.theme.systemAnimationsOff

/**
 * Where the app is. Child side: "start" (decides), "pick" (who plays, FR-PRF-008), "play:<id>" (child mode).
 * Adult side, reached only through "gate" (FR-ADM-001): "adult", "profile:<id>", "settings:<id>", "add:<step>".
 */
private fun adult(route: String) = route == "gate" || route == "adult" || route.startsWith("profile:") || route.startsWith("settings:") || route.startsWith("add")

@Composable
fun NuhaApp() {
    val model: PreferencesViewModel = viewModel()
    val preferences by model.preferences.collectAsStateWithLifecycle()
    val error by model.error.collectAsStateWithLifecycle()
    val profiles: ProfilesViewModel = viewModel()
    val children by profiles.children.collectAsStateWithLifecycle()
    var route by rememberSaveable { mutableStateOf("start") }
    // The child-side route to go back to when the gate is cancelled.
    var back by rememberSaveable { mutableStateOf("start") }
    // Each run of onboarding gets a fresh model, so a second child never starts with the first one's answers.
    var run by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current
    val current = preferences
    // Screens with no child yet follow the device: no motion when its animations are off (FR-SET-004).
    val motion = when { systemAnimationsOff(context) -> Motion.None; current?.reducedMotion != false -> Motion.Reduced; else -> Motion.Full }
    NuhaTheme(appearance = current?.appearance ?: "colourful", motion = motion) {
        val list = children
        when {
            // Wait for the saved appearance so a muted child never sees the colourful default first.
            current == null || list == null -> Placeholder(if (error) "تعذّر تحميل الإعدادات. أعد فتح التطبيق للمحاولة مجدداً." else "")
            // First run, or every child deleted: the child-first onboarding, its child starting at a default sensory point.
            !current.onboardingComplete || (list.isEmpty() && !adult(route)) -> {
                val onboarding: OnboardingViewModel = viewModel(key = "onboarding-$run")
                FirstRun(onboarding, current) { onboarding.save(Pending.OnboardingSensory) { id -> model.finish { route = "play:$id" } } }
            }
            else -> {
                // Logged without any child data (NFR-PRV-006), so a random-input run can show the gate never passed (FR-ADM-002).
                val openGate = { from: String -> Log.i("NuhaGate", "gate opened"); back = from; route = "gate" }
                // Leaving the adult area asks who plays again (or opens the only child); «العب الآن» picks a child directly.
                val leaveAdult = { run++; route = "start" }
                val r = if (route == "start") (if (list.size >= 2) "pick" else "play:${list.first().id}") else route
                val id = r.substringAfter(':', "").toLongOrNull()
                when {
                    r == "pick" -> ChildPicker(list, onPick = { route = "play:${it.id}" }, onAdult = { openGate("pick") }, voice = current.voice, effects = current.effects)
                    r.startsWith("play:") && id != null -> ChildMode(id, onAgain = if (list.size >= 2) ({ route = "pick" }) else null, onAdult = { openGate(r) })
                    r == "gate" -> GateScreen(onPass = { Log.i("NuhaGate", "gate passed"); route = "adult" }, onCancel = { route = back })
                    r == "adult" -> AdultHome(
                        profiles, onPlay = { route = "play:$it" }, onProfile = { route = "profile:$it" }, onSettings = { route = "settings:$it" },
                        onAdd = { run++; route = "add:welcome" }, onExit = leaveAdult,
                    )
                    r.startsWith("profile:") && id != null -> ProfileScreen(profiles, id, onDone = { route = "adult" })
                    r.startsWith("settings:") && id != null -> SettingsScreen(profiles, id, onDone = { route = "adult" })
                    r.startsWith("add") -> AddChild(viewModel(key = "onboarding-$run"), current, step = r.substringAfter(':'), go = { route = it })
                    else -> LaunchedEffect(r) { route = "start" }
                }
            }
        }
    }
}

/** Onboarding steps: welcome, then who's playing, then the observation questions and starting path. */
@Composable
private fun FirstRun(onboarding: OnboardingViewModel, preferences: AppPreferences, onFinish: () -> Unit) {
    var screen by rememberSaveable { mutableStateOf("welcome") }
    when (screen) {
        "welcome" -> WelcomeScreen(onStart = { screen = "who" }, voice = preferences.voice, effects = preferences.effects)
        "who" -> {
            BackHandler { screen = "welcome" }
            WhoScreen(onboarding, onBack = { screen = "welcome" }, onNext = { screen = "questions" }, voice = preferences.voice, effects = preferences.effects)
        }
        else -> {
            BackHandler { if (onboarding.question > 0) onboarding.question-- else screen = "who" }
            QuestionsScreen(onboarding, onBack = { screen = "who" }, onFinish = onFinish, voice = preferences.voice, effects = preferences.effects)
        }
    }
}

/**
 * Adding a child from the adult area re-runs onboarding without touching the other profiles (FR-ONB-012), then asks
 * the adult for a sensory starting point before saving (FR-SET-005).
 */
@Composable
private fun AddChild(onboarding: OnboardingViewModel, preferences: AppPreferences, step: String, go: (String) -> Unit) {
    when (step) {
        "welcome" -> {
            BackHandler { go("adult") }
            WelcomeScreen(onStart = { go("add:who") }, voice = preferences.voice, effects = preferences.effects)
        }
        "who" -> {
            BackHandler { go("add:welcome") }
            WhoScreen(onboarding, onBack = { go("add:welcome") }, onNext = { go("add:questions") }, voice = preferences.voice, effects = preferences.effects)
        }
        "questions" -> {
            BackHandler { if (onboarding.question > 0) onboarding.question-- else go("add:who") }
            QuestionsScreen(onboarding, onBack = { go("add:who") }, onFinish = { go("add:sensory") }, voice = preferences.voice, effects = preferences.effects)
        }
        else -> SensoryScreen(saving = onboarding.saving, error = onboarding.error, onBack = { go("add:questions") }, onSave = { point -> onboarding.save(point) { go("adult") } })
    }
}

@Composable
private fun Placeholder(text: String) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.headlineSmall)
    }
}
