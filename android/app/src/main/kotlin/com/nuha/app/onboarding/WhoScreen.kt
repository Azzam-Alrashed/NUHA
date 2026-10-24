package com.nuha.app.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuha.app.DraftArabic
import com.nuha.app.LocalNumerals
import com.nuha.app.Pending
import com.nuha.app.audio.rememberEffects
import com.nuha.app.audio.rememberSpeech
import com.nuha.app.ui.identity.AvatarKinds
import com.nuha.app.ui.identity.ChunkySurface
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.NuhaAvatar
import com.nuha.app.ui.identity.avatarHue
import com.nuha.app.ui.theme.buttonWeight
import com.nuha.app.ui.theme.titleWeight

@DraftArabic private const val PROMPT = "مَن سيلعب معي؟"
private const val MAX_NAME = 20
internal val AvatarNames = mapOf(
    "squircle" to "مربّع أخضر", "dome" to "قبّة زرقاء", "clover" to "برسيم وردي",
    "sun" to "شمس برتقالية", "bean" to "حبّة بنفسجية", "triangle" to "مثلث أرجواني",
)

/**
 * Onboarding, screen 2: who's playing. The child (with a grown-up for the name) picks a shape buddy as their
 * avatar, and the grown-up enters the name and age. The name sits at the top so the keyboard never covers it.
 */
@Composable
fun WhoScreen(model: OnboardingViewModel, onBack: () -> Unit, onNext: () -> Unit, voice: Boolean, effects: Boolean) {
    val c = LocalIdentity.current
    val numerals = LocalNumerals.current
    val speech = rememberSpeech(voice)
    val sounds = rememberEffects(effects)
    val focus = LocalFocusManager.current
    LaunchedEffect(Unit) { speech?.say(PROMPT) }
    OnboardingFrame(
        step = 1, prompt = PROMPT, primary = "التالي", primaryEnabled = model.childReady,
        onPrimary = { focus.clearFocus(); onNext() }, onBack = { focus.clearFocus(); onBack() },
        speech = speech, effects = sounds,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(36.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CardLabel("الاسم")
                BasicTextField(
                    value = model.name,
                    onValueChange = { model.name = it.take(MAX_NAME) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium.titleWeight().copy(color = c.ink, fontSize = 30.sp),
                    cursorBrush = SolidColor(c.action.fill),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "اسم الطفل" },
                    decorationBox = { field ->
                        ChunkySurface(c.action, Color.White, Modifier.fillMaxWidth().height(84.dp), corner = null, contentAlignment = Alignment.CenterStart) {
                            Box(Modifier.padding(horizontal = 30.dp), contentAlignment = Alignment.CenterStart) {
                                if (model.name.isEmpty()) Text("اسم الطفل", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp), color = c.ink.copy(alpha = .45f))
                                field()
                            }
                        }
                    },
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CardLabel("العمر")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (age in Pending.AgeRange) ChoiceTile(
                        c.action, selected = model.age == age,
                        onSelect = { if (model.age != age) sounds?.pop(); model.age = age },
                        description = "${numerals.format(age)} سنوات", modifier = Modifier.size(80.dp), corner = null,
                    ) { Text(numerals.format(age), style = MaterialTheme.typography.headlineMedium.buttonWeight().copy(fontSize = 34.sp), color = c.ink) }
                }
            }
        }
        CardLabel("اختر صديقك")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (kind in AvatarKinds) ChoiceTile(
                c.avatarHue(kind), selected = model.avatar == kind,
                onSelect = { focus.clearFocus(); if (model.avatar != kind) sounds?.pop(); model.avatar = kind },
                description = AvatarNames[kind] ?: kind, modifier = Modifier.size(124.dp),
            ) { NuhaAvatar(kind, Modifier.size(92.dp), gaze = if (model.avatar == kind) androidx.compose.ui.geometry.Offset(0f, -1f) else androidx.compose.ui.geometry.Offset.Zero) }
        }
    }
}
