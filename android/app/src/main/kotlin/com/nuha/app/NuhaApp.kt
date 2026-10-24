package com.nuha.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nuha.app.data.PreferencesViewModel
import com.nuha.app.ui.theme.NuhaTheme

@Composable
fun NuhaApp() {
    val model: PreferencesViewModel = viewModel()
    val preferences by model.preferences.collectAsStateWithLifecycle()
    val error by model.error.collectAsStateWithLifecycle()
    NuhaTheme(appearance = preferences?.appearance ?: "colourful", reducedMotion = preferences?.reducedMotion ?: true) {
        // Placeholder until the first rebuilt screen lands.
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(if (error) "تعذّر تحميل الإعدادات. أعد فتح التطبيق للمحاولة مجدداً." else "نُهى")
        }
    }
}
