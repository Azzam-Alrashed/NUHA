package com.nuha.app.adult

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.nuha.app.DraftArabic
import com.nuha.app.Pending
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.theme.buttonWeight
import kotlinx.coroutines.launch

/**
 * The way into the adult area from child screens (FR-ADM-001). Mechanism is PLACEHOLDER OI-15: a small «للكبار» pill
 * that opens the gate only after an unbroken hold of [Pending.GateHoldMillis]; a ring fills while it is held, and any
 * lift or slide cancels it. TalkBack users get the same through the long-press action (FR-ADM-005). Holding only opens
 * the gate; the gate then asks for a number.
 */
@Composable
fun AdultEntry(onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalIdentity.current
    val progress = remember { Animatable(0f) }
    val open by rememberUpdatedState(onOpen)
    val scope = rememberCoroutineScope()
    Box(
        modifier
            .heightIn(min = 72.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = AdultCopy.EntryDescription
                onLongClick(AdultCopy.EntryAction) { open(); true }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val hold = scope.launch {
                        progress.snapTo(0f)
                        progress.animateTo(1f, tween(Pending.GateHoldMillis.toInt(), easing = LinearEasing))
                        open()
                    }
                    while (true) {
                        val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed || (change.position - down.position).getDistance() > viewConfiguration.touchSlop) break
                    }
                    hold.cancel()
                    scope.launch { progress.snapTo(0f) }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier.background(Color.White.copy(alpha = .92f), CircleShape).border(2.dp, c.ink.copy(alpha = .22f), CircleShape).padding(start = 12.dp, end = 18.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier.size(32.dp).drawBehind {
                    val stroke = 3.dp.toPx()
                    drawCircle(c.ink.copy(alpha = .15f), size.minDimension / 2 - stroke / 2, style = Stroke(stroke))
                    drawArc(c.action.lip, -90f, 360f * progress.value, false, Offset(stroke / 2, stroke / 2), Size(size.width - stroke, size.height - stroke), style = Stroke(stroke, cap = StrokeCap.Round))
                    // A small padlock.
                    val w = size.width * .36f; val h = size.height * .28f
                    val left = (size.width - w) / 2; val top = size.height * .46f
                    drawArc(c.ink, 180f, 180f, false, Offset(left + w * .18f, top - h * .75f), Size(w * .64f, h * 1.3f), style = Stroke(2.2.dp.toPx()))
                    drawRoundRect(c.ink, Offset(left, top), Size(w, h), CornerRadius(2.dp.toPx()))
                },
            )
            Text(AdultCopy.Entry, style = MaterialTheme.typography.titleMedium.buttonWeight(), color = c.ink)
        }
    }
}

/** Arabic number words for 1–999, shown by the gate. */
@DraftArabic
object NumberWords {
    private val Units = listOf("", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة", "ستة", "سبعة", "ثمانية", "تسعة")
    private val Teens = listOf("عشرة", "أحد عشر", "اثنا عشر", "ثلاثة عشر", "أربعة عشر", "خمسة عشر", "ستة عشر", "سبعة عشر", "ثمانية عشر", "تسعة عشر")
    private val Tens = listOf("", "", "عشرون", "ثلاثون", "أربعون", "خمسون", "ستون", "سبعون", "ثمانون", "تسعون")
    private val Hundreds = listOf("", "مئة", "مئتان", "ثلاثمئة", "أربعمئة", "خمسمئة", "ستمئة", "سبعمئة", "ثمانمئة", "تسعمئة")

    fun of(n: Int): String {
        require(n in 1..999)
        val r = n % 100
        val rest = when {
            r == 0 -> ""
            r < 10 -> Units[r]
            r < 20 -> Teens[r - 10]
            r % 10 == 0 -> Tens[r / 10]
            else -> "${Units[r % 10]} و${Tens[r / 10]}"
        }
        return listOf(Hundreds[n / 100], rest).filter { it.isNotEmpty() }.joinToString(" و")
    }
}

enum class GateResult { Typing, Pass, Fail }

/** Whether [typed] digits open the gate for [number]: still typing, right, or wrong (which closes the gate). */
fun gateResult(number: Int, typed: String): GateResult = when {
    typed.length < number.toString().length -> GateResult.Typing
    typed == number.toString() -> GateResult.Pass
    else -> GateResult.Fail
}

/**
 * The gate (FR-ADM-002, PLACEHOLDER OI-15): type a number shown in words. Reading the words is the deliberate adult
 * action; one wrong number closes the gate, so random input must hold the entry again before another try. It is not
 * described or used as guardianship verification or consent (FR-ADM-003).
 */
@Composable
fun GateScreen(onPass: () -> Unit, onCancel: () -> Unit) {
    val number = rememberSaveable { Pending.GateNumbers.random() }
    var typed by rememberSaveable { mutableStateOf("") }
    val digits = Pending.DefaultNumerals
    BackHandler(onBack = onCancel)
    AdultTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)) {
                Text(AdultCopy.GateTitle, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
                Text(AdultCopy.GateNote, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(AdultCopy.GatePrompt, style = MaterialTheme.typography.titleLarge)
                Text(NumberWords.of(number), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                // Numbers are written left to right, and so is a keypad.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            repeat(number.toString().length) { i ->
                                Box(
                                    Modifier.size(64.dp).border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center,
                                ) { Text(typed.getOrNull(i)?.let { digits.format(it - '0') }.orEmpty(), style = MaterialTheme.typography.headlineMedium) }
                            }
                        }
                        val keys = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, -1, 0, -2)
                        for (row in keys.chunked(3)) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (k in row) {
                                if (k == -1) { Box(Modifier.size(96.dp, 64.dp)); continue }
                                FilledTonalButton(
                                    onClick = {
                                        if (k == -2) { typed = typed.dropLast(1); return@FilledTonalButton }
                                        typed += k
                                        when (gateResult(number, typed)) {
                                            GateResult.Pass -> onPass()
                                            GateResult.Fail -> onCancel()
                                            GateResult.Typing -> Unit
                                        }
                                    },
                                    modifier = Modifier.size(96.dp, 64.dp).then(if (k == -2) Modifier.semantics { contentDescription = AdultCopy.DeleteDigit } else Modifier),
                                    shape = RoundedCornerShape(16.dp),
                                ) { Text(if (k == -2) "⌫" else digits.format(k), style = MaterialTheme.typography.headlineSmall) }
                            }
                        }
                    }
                }
                TextButton(onClick = onCancel) { Text(AdultCopy.Cancel, style = MaterialTheme.typography.titleMedium) }
            }
        }
    }
}
