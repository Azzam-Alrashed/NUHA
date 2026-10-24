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
import com.nuha.app.onboarding.WelcomeScreen
import com.nuha.app.ui.theme.NuhaTheme

@Composable
fun NuhaApp() {
    val model: PreferencesViewModel = viewModel()
    val preferences by model.preferences.collectAsStateWithLifecycle()
    val error by model.error.collectAsStateWithLifecycle()
    var screen by rememberSaveable { mutableStateOf("welcome") }
    NuhaTheme(appearance = preferences?.appearance ?: "colourful", reducedMotion = preferences?.reducedMotion ?: true) {
        val current = preferences
        when {
            // Wait for the saved appearance so a muted child never sees the colourful default first.
            current == null -> Placeholder(if (error) "تعذّر تحميل الإعدادات. أعد فتح التطبيق للمحاولة مجدداً." else "")
            screen == "welcome" -> WelcomeScreen(onStart = { screen = "next" }, voice = current.voice, effects = current.effects)
            else -> {
                BackHandler { screen = "welcome" }
                Placeholder("الشاشة التالية قيد البناء")
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
