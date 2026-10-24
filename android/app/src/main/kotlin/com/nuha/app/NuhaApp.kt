package com.nuha.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nuha.app.data.PreferencesViewModel
import com.nuha.app.onboarding.OnboardingViewModel
import com.nuha.app.onboarding.QuestionsScreen
import com.nuha.app.onboarding.WelcomeScreen
import com.nuha.app.onboarding.WhoScreen
import com.nuha.app.ui.theme.NuhaTheme

@Composable
fun NuhaApp() {
    val model: PreferencesViewModel = viewModel()
    val preferences by model.preferences.collectAsStateWithLifecycle()
    val error by model.error.collectAsStateWithLifecycle()
    val onboarding: OnboardingViewModel = viewModel()
    // Onboarding steps: welcome, then who's playing, then the observation questions and starting path.
    var screen by rememberSaveable { mutableStateOf("welcome") }
    NuhaTheme(appearance = preferences?.appearance ?: "colourful", reducedMotion = preferences?.reducedMotion ?: true) {
        val current = preferences
        when {
            // Wait for the saved appearance so a muted child never sees the colourful default first.
            current == null -> Placeholder(if (error) "تعذّر تحميل الإعدادات. أعد فتح التطبيق للمحاولة مجدداً." else "")
            current.onboardingComplete -> Placeholder("الخريطة قيد البناء")
            screen == "welcome" -> WelcomeScreen(onStart = { screen = "who" }, voice = current.voice, effects = current.effects)
            screen == "who" -> {
                BackHandler { screen = "welcome" }
                WhoScreen(onboarding, onBack = { screen = "welcome" }, onNext = { screen = "questions" }, voice = current.voice, effects = current.effects)
            }
            else -> {
                BackHandler { if (onboarding.question > 0) onboarding.question-- else screen = "who" }
                QuestionsScreen(
                    onboarding, onBack = { screen = "who" },
                    onFinish = { onboarding.save { model.finish() } },
                    voice = current.voice, effects = current.effects,
                )
            }
        }
    }
}

@Composable
private fun Placeholder(text: String) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.headlineSmall)
    }
}
