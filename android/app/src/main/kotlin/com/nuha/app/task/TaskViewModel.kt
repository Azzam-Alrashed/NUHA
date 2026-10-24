package com.nuha.app.task

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nuha.app.Pending
import com.nuha.app.child.ChildCopy
import com.nuha.app.data.AppDatabase
import com.nuha.app.data.SessionRecord
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface TaskPhase {
    data object Loading : TaskPhase
    /** An interrupted session with saved trials: the child chooses to continue or start over (FR-ACT-010). */
    data class OfferResume(val session: SessionRecord, val saved: Int) : TaskPhase
    data object Playing : TaskPhase
    /** The token board is full (FR-RWD-003). */
    data object Celebrating : TaskPhase
}

/** Something to say or play once; [id] makes a repeat of the same line a new event. */
data class Cue(val text: String, val id: Int)
/** A star flying from where the child answered ([from], in root coordinates, or null) to board slot [slot]. */
data class Flight(val from: Offset?, val slot: Int, val id: Int)

/**
 * Screen state for one activity session, driving [SessionEngine]. It speaks the instruction when a trial starts,
 * steps up the prompt ladder after a wrong answer, a help request or a quiet spell, and paces the per-trial
 * celebration (FR-RWD-002) before the next trial.
 */
class TaskViewModel(application: Application, private val plan: ActivityPlan, childId: Long, private val childName: String) : AndroidViewModel(application) {
    private val engine = SessionEngine(RoomRecordStore(AppDatabase.get(application).records()), plan, childId)

    var phase by mutableStateOf<TaskPhase>(TaskPhase.Loading); private set
    var trial by mutableStateOf<TrialSpec?>(null); private set
    /** Changes with every trial, so trial-scoped UI state resets. */
    var trialNumber by mutableIntStateOf(0); private set
    var tokens by mutableIntStateOf(0); private set
    var level by mutableIntStateOf(Ladder.None); private set
    var solvedKey by mutableStateOf<String?>(null); private set
    var wrongKey by mutableStateOf<String?>(null); private set
    var wrongTick by mutableIntStateOf(0); private set
    var nodTick by mutableIntStateOf(0); private set
    var flight by mutableStateOf<Flight?>(null); private set
    var speech by mutableStateOf<Cue?>(null); private set
    var sound by mutableStateOf<Cue?>(null); private set
    val slots: Int get() = plan.slots

    private var cues = 0
    private var paused = false
    private var background = false
    private var busy = false
    private var prompter: Job? = null

    /** Called when the activity opens: offer to resume an interrupted session, or start a new one. */
    fun open() {
        phase = TaskPhase.Loading
        viewModelScope.launch {
            val interrupted = engine.interrupted()
            if (interrupted != null && interrupted.second > 0) phase = TaskPhase.OfferResume(interrupted.first, interrupted.second)
            else { engine.startNew(); beginTrial() }
        }
    }

    fun resume(session: SessionRecord) = viewModelScope.launch { engine.resume(session); beginTrial() }
    fun startOver() = viewModelScope.launch { engine.startNew(); beginTrial() }
    fun playAgain() = startOver()

    private fun beginTrial() {
        val spec = engine.current ?: return
        trial = spec; trialNumber++; tokens = engine.tokens; level = engine.tracker?.level ?: Ladder.None
        solvedKey = null; wrongKey = null; busy = false
        phase = TaskPhase.Playing
        engine.tracker?.let { if (paused || background) it.pause() }
        say(promptFor(level, spec))
        startPrompter()
    }

    /**
     * A response from the activity. Correct: save, fill a slot, celebrate, then move on. Wrong: the ladder steps up
     * and the matching prompt is spoken; nothing is lost and there is no failure state (FR-ACT-007).
     */
    fun respond(choice: String, correct: Boolean, input: InputType, at: Offset?, reducedMotion: Boolean, strokes: String? = null) {
        if (phase != TaskPhase.Playing || busy || solvedKey != null) return
        busy = true
        viewModelScope.launch {
            val spec = trial ?: return@launch
            val solved = engine.respond(choice, correct, input, strokes)
            level = engine.tracker?.level ?: level
            if (!solved) {
                wrongKey = choice; wrongTick++
                say(promptFor(level, spec))
                busy = false
                return@launch
            }
            solvedKey = choice
            tokens = engine.tokens
            flight = Flight(at, tokens - 1, ++cues)
            nodTick++
            play("chime")
            say(spec.praise)
            // Per-trial celebration stays within 1.5 s (FR-RWD-002), then the next trial or the session celebration.
            delay(if (reducedMotion) 1_000 else 1_400)
            engine.advance()
            if (engine.complete) {
                prompter?.cancel()
                phase = TaskPhase.Celebrating
                play("fanfare")
                say(ChildCopy.celebrate(childName))
            } else beginTrial()
        }
    }

    /** The child pressed help: one step up the ladder, counted as a request. */
    fun help() {
        val t = engine.tracker ?: return
        if (phase != TaskPhase.Playing || t.solved) return
        t.stepUp(requested = true)
        level = t.level
        say(promptFor(level, trial ?: return))
    }

    /** Hear the instruction again; not counted as help. */
    fun repeat() { trial?.let { say(it.instruction) } }

    fun setPaused(value: Boolean) { paused = value; syncClock() }
    fun setBackground(value: Boolean) { background = value; syncClock() }
    private fun syncClock() { engine.tracker?.let { if (paused || background) it.pause() else it.resume() } }

    /** The child left (map button, «انتهيت», Back): saved trials stay and the session closes as stopped. */
    fun stop() {
        prompter?.cancel()
        viewModelScope.launch { engine.stop() }
        phase = TaskPhase.Loading; trial = null
    }

    fun flightLanded(id: Int) { if (flight?.id == id) flight = null }

    /** After [Pending.PromptDelayMillis] of quiet active time, the ladder steps up by itself. */
    private fun startPrompter() {
        prompter?.cancel()
        prompter = viewModelScope.launch {
            while (isActive) {
                delay(250)
                val t = engine.tracker ?: continue
                if (paused || background || t.solved || busy || t.level >= Ladder.Model) continue
                if (t.quietMillis() >= Pending.PromptDelayMillis) {
                    t.stepUp(requested = false)
                    level = t.level
                    trial?.let { say(promptFor(level, it)) }
                }
            }
        }
    }

    private fun promptFor(level: Int, spec: TrialSpec) = when {
        level >= Ladder.Model -> spec.model
        level >= Ladder.Hint -> "${ChildCopy.HintLine} ${spec.instruction}"
        else -> spec.instruction
    }

    private fun say(text: String) { speech = Cue(text, ++cues) }
    private fun play(name: String) { sound = Cue(name, ++cues) }
}
