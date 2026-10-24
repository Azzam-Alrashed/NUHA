package com.nuha.app.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuha.app.DraftArabic
import com.nuha.app.LocalNumerals
import com.nuha.app.Pending
import com.nuha.app.audio.rememberEffects
import com.nuha.app.audio.rememberSpeech
import com.nuha.app.ui.identity.ChunkySurface
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.NuhaAvatar
import com.nuha.app.ui.identity.NuhaPictogram
import com.nuha.app.ui.theme.buttonWeight
import com.nuha.app.ui.theme.labelWeight
import com.nuha.app.ui.theme.titleWeight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@DraftArabic private const val QUESTIONS_PROMPT = "أسئلة للكبار"
@DraftArabic private const val RESULT_PROMPT = "وجدنا البداية!"
/** Pause after an answer so the grown-up sees it selected before the next question slides in. */
private const val ADVANCE_DELAY_MILLIS = 350L

/**
 * Onboarding, screen 3: the grown-up answers the observation questions one at a time, then sees the starting
 * path they suggest. Answering moves on by itself; back steps through earlier questions before leaving the step.
 */
@Composable
fun QuestionsScreen(model: OnboardingViewModel, onBack: () -> Unit, onFinish: () -> Unit, voice: Boolean, effects: Boolean) {
    val speech = rememberSpeech(voice)
    val sounds = rememberEffects(effects)
    val scope = rememberCoroutineScope()
    val index = model.question
    val result = index >= Questions.size
    LaunchedEffect(index) { speech?.say(if (result) RESULT_PROMPT else Questions[index].text) }
    OnboardingFrame(
        step = 2,
        prompt = if (result) RESULT_PROMPT else QUESTIONS_PROMPT,
        primary = if (result) "لنبدأ الرحلة!" else "التالي",
        primaryEnabled = if (result) !model.saving else model.answers[index] != null,
        onPrimary = { if (result) onFinish() else model.question = index + 1 },
        onBack = { if (index > 0) model.question = index - 1 else onBack() },
        speech = speech, effects = sounds,
    ) {
        if (!result) QuestionCard(model, index, onAnswer = { answer ->
            if (model.answers[index] != answer) sounds?.pop()
            model.answers[index] = answer
            scope.launch { delay(ADVANCE_DELAY_MILLIS); if (model.question == index) model.question = index + 1 }
        }) else ResultCard(model)
    }
}

@Composable
private fun QuestionCard(model: OnboardingViewModel, index: Int, onAnswer: (Answer) -> Unit) {
    val c = LocalIdentity.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("السؤال ${LocalNumerals.current.format(index + 1)} من ${LocalNumerals.current.format(Questions.size)}", style = MaterialTheme.typography.titleMedium.labelWeight(), color = c.ink)
        // One segment per question: answered ones filled, the current one wider.
        Row(Modifier.clearAndSetSemantics {}, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            for (i in Questions.indices) Box(
                Modifier.height(12.dp).width(if (i == index) 36.dp else 22.dp)
                    .background(if (model.answers[i] != null || i == index) c.action.fill else c.scene.tileLip, CircleShape),
            )
        }
    }
    Text(
        Questions[index].text,
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 52.sp),
        color = c.ink,
        minLines = 2,
        modifier = Modifier.fillMaxWidth().heightIn(min = 104.dp),
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        for (answer in Answer.entries) ChoiceTile(
            c.action, selected = model.answers[index] == answer, onSelect = { onAnswer(answer) },
            description = answer.label, modifier = Modifier.weight(1f).height(100.dp), corner = null,
        ) { Text(answer.label, style = MaterialTheme.typography.headlineMedium.buttonWeight().copy(fontSize = 32.sp), color = c.ink) }
    }
    Text(
        "أجب بحسب ما تلاحظه عادةً على طفلك. تساعدنا الإجابات في اختيار نقطة البداية، ولا تشخّص أي حالة.",
        style = MaterialTheme.typography.bodyLarge, color = c.ink.copy(alpha = .78f),
    )
}

@Composable
private fun ResultCard(model: OnboardingViewModel) {
    val c = LocalIdentity.current
    val path = model.path
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp), verticalAlignment = Alignment.CenterVertically) {
        if (path == StartPath.Mixed) Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (p in listOf(StartPath.Communication, StartPath.Attention, StartPath.Learning)) PathTile(p, 76.dp)
        } else PathTile(path, 200.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("رحلة ${model.name.trim()} تبدأ مع", style = MaterialTheme.typography.titleLarge.labelWeight(), color = c.ink)
            Text(path.label, style = MaterialTheme.typography.displaySmall.titleWeight().copy(fontSize = 52.sp, lineHeight = 76.sp), color = c.ink)
            Text(path.reason, style = MaterialTheme.typography.bodyLarge, color = c.ink.copy(alpha = .78f))
        }
        model.avatar?.let { NuhaAvatar(it, Modifier.size(170.dp), gaze = Offset(1f, 0f)) }
    }
    Text(
        if (model.error) "تعذّر الحفظ. حاول مرة أخرى." else "هذه نقطة بداية مقترحة فقط، ويمكن تغييرها لاحقًا.",
        style = MaterialTheme.typography.bodyLarge, color = c.ink.copy(alpha = .78f),
    )
}

/** A chunky tile in the path's hue with its pictogram, like a stop on the road. */
@Composable
private fun PathTile(path: StartPath, size: Dp) {
    val c = LocalIdentity.current
    val hue = c.pathHue(path)
    ChunkySurface(hue, hue.fill, Modifier.size(size).semantics { contentDescription = path.label }, lip = size * .04f, corner = size * .28f) {
        path.pictogram?.let { NuhaPictogram(it, hue.onFill, Modifier.size(size * .58f)) }
    }
}

