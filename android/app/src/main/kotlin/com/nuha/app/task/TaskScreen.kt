package com.nuha.app.task

import android.app.Application
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nuha.app.LocalNumerals
import com.nuha.app.Pending
import com.nuha.app.audio.rememberEffects
import com.nuha.app.audio.rememberSpeech
import com.nuha.app.child.Activity
import com.nuha.app.child.ActivityFrame
import com.nuha.app.child.ChildCopy
import com.nuha.app.child.LocalChildSettings
import com.nuha.app.child.RoundIconButton
import com.nuha.app.child.activityHue
import com.nuha.app.data.ChildProfile
import com.nuha.app.ui.identity.ChunkyButton
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.LocalMotion
import com.nuha.app.ui.identity.LocalReducedMotion
import com.nuha.app.ui.identity.Motion
import com.nuha.app.ui.identity.NuhaAvatar
import com.nuha.app.ui.identity.NuhaGuide
import com.nuha.app.ui.identity.starPath
import com.nuha.app.ui.theme.buttonWeight
import com.nuha.app.ui.theme.titleWeight
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/** What an activity's content sees of the running trial, and how it answers. */
@Stable
class TaskScope(
    val spec: TrialSpec,
    val level: Int,
    val solvedKey: String?,
    val wrongKey: String?,
    val wrongTick: Int,
    val trialNumber: Int,
    val reducedMotion: Boolean,
    private val onRespond: (choice: String, correct: Boolean, input: InputType, at: Offset?, strokes: String?, detail: String?) -> Unit,
    private val onProgress: (choice: String, input: InputType) -> Unit,
    private val onMiss: () -> Unit,
    private val onActive: () -> Unit,
    private val onSay: (String) -> Unit,
) {
    val solved: Boolean get() = solvedKey != null
    /**
     * Report a response. [at] is where it happened, in root coordinates, so the star can fly from there. [strokes]: the
     * drawing (kept as an attempt when wrong). [detail]: activity-specific measures saved with the solved trial.
     */
    fun respond(choice: String, correct: Boolean, input: InputType, at: Offset?, strokes: String? = null, detail: String? = null) = onRespond(choice, correct, input, at, strokes, detail)
    /** A correct step of a multi-step trial that does not finish it. */
    fun progress(choice: String, input: InputType) = onProgress(choice, input)
    /** An input that hit no target (a missed drop, an obstacle touched); recorded apart from wrong answers. */
    fun miss() = onMiss()
    /** The child is working (touching, drawing); holds off the automatic prompt for a quiet spell. */
    fun active() = onActive()
    /** Speak a line that is not an answer, such as a group's count when the child inspects it. */
    fun say(text: String) = onSay(text)
}

/**
 * The shared task screen (FR-ACT-002, -003): one instruction with the activity's mascot, a repeat button and a help
 * button, the task, the token board in the top bar, and pause, all visible together without scrolling.
 */
@Composable
fun TaskScreen(
    activity: Activity,
    child: ChildProfile,
    plans: (completedSessions: Int) -> ActivityPlan,
    voice: Boolean,
    effects: Boolean,
    onMap: () -> Unit,
    content: @Composable BoxScope.(TaskScope) -> Unit,
) {
    val app = LocalContext.current.applicationContext as Application
    val vm: TaskViewModel = viewModel(key = "task-${child.id}-${activity.id}") { TaskViewModel(app, activity.id, plans, child.id, child.name) }
    val reduced = LocalReducedMotion.current
    val settings = LocalChildSettings.current
    // Off keeps the token board filling but drops the flight, chime, nod and fanfare (FR-RWD-005).
    val celebrate = settings.celebration != "off"
    SideEffect { vm.promptDelayMillis = settings.promptDelaySeconds.takeIf { it > 0 }?.times(1_000L) }
    val speech = rememberSpeech(voice)
    val sounds = rememberEffects(effects)
    LaunchedEffect(Unit) { vm.open() }
    LaunchedEffect(vm.speech?.id) { vm.speech?.let { speech?.say(it.text) } }
    LaunchedEffect(vm.sound?.id) {
        if (celebrate) when (vm.sound?.text) { "chime" -> sounds?.chime(); "fanfare" -> sounds?.fanfare() }
    }
    // The clock stops while the app is in the background, as it does on pause.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_STOP) vm.setBackground(true) else if (e == Lifecycle.Event.ON_START) vm.setBackground(false)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val leave = { vm.stop(); onMap() }
    // System Back leaves the activity like the map button: the session stops, saved trials stay.
    androidx.activity.compose.BackHandler { leave() }
    if (vm.phase == TaskPhase.Celebrating) {
        SessionCelebration(child, vm.tokens, vm.slots, onAgain = { vm.playAgain() }, onMap = leave, effects = effects)
        return
    }
    val slotCenters = remember { mutableStateMapOf<Int, Offset>() }
    val flying = vm.flight?.takeIf { !reduced && celebrate && it.from != null }
    ActivityFrame(
        activity, onMap = leave, effects = effects,
        topCenter = { TokenBoard(filled = vm.tokens - if (flying != null) 1 else 0, vm.slots) { i, c -> slotCenters[i] = c } },
        overlay = {
            flying?.let { f -> StarFlight(f, slotCenters[f.slot]) { vm.flightLanded(f.id) } }
            (vm.phase as? TaskPhase.OfferResume)?.let { offer ->
                ResumeCard(offer.saved, vm.slots, sounds, onResume = { vm.resume(offer.session) }, onStartOver = { vm.startOver() })
            }
        },
    ) { paused ->
        LaunchedEffect(paused) { vm.setPaused(paused) }
        val spec = vm.trial
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InstructionStrip(activity, spec?.instruction.orEmpty(), vm.nodTick, settings.mascot, settings.celebration, sounds, onRepeat = vm::repeat, onHelp = vm::help)
            Box(Modifier.weight(1f).fillMaxWidth().watchTouches(vm::active), contentAlignment = Alignment.Center) {
                if (spec != null && vm.phase == TaskPhase.Playing) {
                    val scope = TaskScope(
                        spec, vm.level, vm.solvedKey, vm.wrongKey, vm.wrongTick, vm.trialNumber, reduced,
                        onRespond = { choice, correct, input, at, strokes, detail -> vm.respond(choice, correct, input, at, reduced, strokes, detail) },
                        onProgress = vm::progress, onMiss = vm::miss, onActive = vm::active, onSay = vm::sayLine,
                    )
                    // Target size scales everything in the play area, so targets and gaps grow together (FR-SET-002).
                    val density = LocalDensity.current
                    val scale = Pending.TargetScales[settings.targetSize] ?: 1f
                    CompositionLocalProvider(LocalDensity provides Density(density.density * scale, density.fontScale)) { content(scope) }
                }
            }
        }
    }
}

/**
 * The mascot, the one instruction in a bubble, and the repeat and help buttons. [mascot] is the child's setting: the
 * activity's own buddy, the lead guide (FR-ACT-016) or none. The mascot nods on success, twice as far when lively.
 */
@Composable
private fun InstructionStrip(
    activity: Activity, instruction: String, nodTick: Int, mascot: String, celebration: String,
    sounds: com.nuha.app.audio.Effects?, onRepeat: () -> Unit, onHelp: () -> Unit,
) {
    val c = LocalIdentity.current
    val reduced = LocalReducedMotion.current
    val hue = c.activityHue(activity)
    val nod = remember { Animatable(0f) }
    LaunchedEffect(nodTick) {
        val k = when (celebration) { "off" -> 0f; "lively" -> 2f; else -> 1f }
        if (nodTick > 0 && !reduced && k > 0f) nod.animateTo(0f, keyframes { durationMillis = 520; 9f * k at 130; -6f * k at 300; 3f * k at 420; 0f at 520 })
    }
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        val nodding = Modifier.size(104.dp).graphicsLayer { rotationZ = nod.value }
        when (mascot) {
            "guide" -> NuhaGuide(nodding, description = Pending.GuideName)
            "off" -> Unit
            else -> NuhaAvatar(activity.mascot, nodding, hue = hue)
        }
        Box(
            Modifier
                .weight(1f)
                .background(hue.tint, RoundedCornerShape(36.dp))
                .padding(horizontal = 32.dp, vertical = 14.dp)
                .semantics { heading(); liveRegion = LiveRegionMode.Polite },
        ) {
            Text(instruction, color = c.ink, style = MaterialTheme.typography.headlineMedium.titleWeight().copy(fontSize = 32.sp, lineHeight = 48.sp))
        }
        RoundIconButton("speaker", ChildCopy.Repeat, c.action, sounds, onRepeat)
        RoundIconButton("help", ChildCopy.Help, c.action, sounds, onHelp)
    }
}

/** 3–5 star slots (FR-RWD-001); each reports its centre so a star can fly into it. Under reduced motion new stars fade in; under none they appear. */
@Composable
fun TokenBoard(filled: Int, slots: Int, onSlotCenter: (Int, Offset) -> Unit) {
    val numerals = LocalNumerals.current
    val motion = LocalMotion.current
    val c = LocalIdentity.current
    Row(
        Modifier
            .background(Color.White, CircleShape)
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .clearAndSetSemantics { contentDescription = ChildCopy.board(filled.coerceAtLeast(0), slots, numerals) },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(slots) { i ->
            val on by animateFloatAsState(if (i < filled) 1f else 0f, if (motion == Motion.None) snap() else tween(250), label = "Slot $i")
            Canvas(Modifier.size(48.dp).onGloballyPositioned { onSlotCenter(i, it.positionInRoot() + Offset(it.size.width / 2f, it.size.height / 2f)) }) {
                val path = starPath(center, size.minDimension / 2)
                if (!c.highContrast) drawPath(path, c.scene.tileLip.copy(alpha = .45f))
                drawPath(path, c.scene.tileLip, style = Stroke(2.dp.toPx(), join = StrokeJoin.Round))
                if (on > 0f) {
                    drawPath(path, c.texasRose.fill.copy(alpha = on))
                    drawPath(path, c.texasRose.lip.copy(alpha = on), style = Stroke(2.dp.toPx(), join = StrokeJoin.Round))
                }
            }
        }
    }
}

/** A star arcing from the answer to its slot in about 650 ms, landing big and settling (FR-RWD-002). */
@Composable
private fun BoxScope.StarFlight(flight: Flight, to: Offset?, onLanded: () -> Unit) {
    val c = LocalIdentity.current
    val from = flight.from ?: return
    val target = to ?: return
    val t = remember(flight.id) { Animatable(0f) }
    LaunchedEffect(flight.id) { t.animateTo(1f, tween(650, easing = FastOutSlowInEasing)); onLanded() }
    val px = with(LocalDensity.current) { 64.dp.toPx() }
    val lift = with(LocalDensity.current) { 120.dp.toPx() }
    val p = from + (target - from) * t.value - Offset(0f, lift * sin(PI * t.value).toFloat())
    Canvas(
        Modifier
            .align(androidx.compose.ui.AbsoluteAlignment.TopLeft)
            .absoluteOffset { IntOffset((p.x - px / 2).roundToInt(), (p.y - px / 2).roundToInt()) }
            .size(64.dp)
            .graphicsLayer { val s = 1.5f - .5f * t.value; scaleX = s; scaleY = s }
            .clearAndSetSemantics {},
    ) {
        val path = starPath(center, size.minDimension / 2)
        drawPath(path, c.texasRose.fill)
        drawPath(path, c.texasRose.lip, style = Stroke(2.5.dp.toPx(), join = StrokeJoin.Round))
    }
}

/** After an interruption: continue the saved session or start a new one (FR-ACT-010). */
@Composable
private fun ResumeCard(saved: Int, slots: Int, sounds: com.nuha.app.audio.Effects?, onResume: () -> Unit, onStartOver: () -> Unit) {
    val c = LocalIdentity.current
    Box(Modifier.fillMaxSize().background(c.ink.copy(alpha = .55f)).pointerBlock(), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .widthIn(min = 640.dp)
                .drawBehind {
                    val lip = 8.dp.toPx()
                    val face = Size(size.width, size.height - lip)
                    val r = CornerRadius(40.dp.toPx())
                    drawRoundRect(c.scene.tileLip, Offset(0f, lip), face, r)
                    drawRoundRect(Color.White, Offset.Zero, face, r)
                }
                .padding(start = 48.dp, end = 48.dp, top = 24.dp, bottom = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            NuhaGuide(Modifier.size(170.dp), gaze = Offset(0f, .6f))
            Text(ChildCopy.ResumeQuestion, color = c.ink, style = MaterialTheme.typography.displaySmall.titleWeight().copy(fontSize = 44.sp, lineHeight = 64.sp), modifier = Modifier.semantics { heading() })
            Text(ChildCopy.resumeSaved(saved, slots, LocalNumerals.current), color = c.ink, style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                val style = MaterialTheme.typography.displaySmall.buttonWeight().copy(fontSize = 34.sp, lineHeight = 46.sp)
                ChunkyButton(ChildCopy.Resume, { sounds?.pop(); onResume() }, Modifier.height(104.dp).widthIn(min = 240.dp), style = style)
                ChunkyButton(ChildCopy.StartOver, { sounds?.pop(); onStartOver() }, Modifier.height(104.dp).widthIn(min = 240.dp), secondary = true, style = style)
            }
        }
    }
}

/**
 * Calls [onTouch] for every press and move inside, without consuming anything: a child who is touching or drawing is
 * working, so the quiet spell before an automatic prompt starts again (however long a stroke takes).
 */
private fun Modifier.watchTouches(onTouch: () -> Unit) = pointerInput(Unit) {
    awaitPointerEventScope { while (true) { if (awaitPointerEvent(PointerEventPass.Initial).changes.any { it.pressed }) onTouch() } }
}

/** Swallows touches so nothing under a card can be pressed. */
internal fun Modifier.pointerBlock() = pointerInput(Unit) {
    awaitPointerEventScope { while (true) { awaitPointerEvent().changes.forEach { it.consume() } } }
}

