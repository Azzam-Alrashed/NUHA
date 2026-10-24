package com.nuha.app.task

import com.nuha.app.DraftArabic
import com.nuha.app.Pending
import com.nuha.app.child.Activity
import com.nuha.app.data.RecordsDao
import com.nuha.app.data.SessionRecord
import com.nuha.app.data.TrialRecord
import java.util.UUID

// The activity framework's core, free of Android UI so it can be unit-tested (NFR-MNT-003).

/** How a response arrived; recorded with every trial (FR-ACT-011, IR-HW-001). */
enum class InputType(val id: String) { Stylus("stylus"), Finger("finger"), Other("other") }

/**
 * Prompt-ladder levels (FR-ACT-007). A wrong answer, a request for help, or a quiet spell steps up one level;
 * the next trial starts lower again (fading, [Pending.fadedStart]).
 */
object Ladder {
    const val None = 0
    /** Repeat the instruction. */
    const val Repeat = 1
    /** Highlight where to look. */
    const val Hint = 2
    /** Show the answer; the child still makes the response. */
    const val Model = 3
}

/** One trial's content. Activities build these; the framework runs them. [item] is the activity's own data. */
class TrialSpec(
    val key: String,
    @DraftArabic val instruction: String,
    @DraftArabic val praise: String,
    @DraftArabic val model: String,
    val item: Any,
)

/** What an activity gives the framework: its identity, content version, difficulty settings and trials. */
interface ActivityPlan {
    val activity: Activity
    val taskVersion: Int
    /** The active difficulty settings, stored with each trial (FR-ACT-012), e.g. "level=1;options=3". */
    val difficulty: String
    val slots: Int get() = Pending.TokenSlots
    /** Exactly [slots] trials, the same ones for the same [seed], so a resumed session continues where it stopped. */
    fun trials(seed: Long): List<TrialSpec>
}

/**
 * Tracks one trial from its start: the first response and whether it came before any prompt, wrong attempts,
 * the prompt level, help requests, and active time excluding pauses.
 */
class TrialTracker(val id: String, startLevel: Int, private val clock: () -> Long) {
    val startedAt = clock()
    var level = startLevel.coerceIn(Ladder.None, Ladder.Model); private set
    var maxLevel = level; private set
    var helpRequests = 0; private set
    var wrongAttempts = 0; private set
    var firstChoice: String? = null; private set
    var firstCorrect = false; private set
    var firstIndependent = false; private set
    var input = InputType.Other; private set
    var solved = false; private set
    var misses = 0; private set
    /** Drawing attempts that did not solve the trial, in order (encoded strokes). */
    val attempts = mutableListOf<String>()
    private var active = 0L
    private var runningSince: Long? = clock()
    /** Active time when the level last changed, for automatic prompts after a quiet spell. */
    private var levelChangedAt = 0L

    fun pause() { runningSince?.let { active += clock() - it }; runningSince = null }
    fun resume() { if (runningSince == null && !solved) runningSince = clock() }
    fun activeMillis(): Long = active + (runningSince?.let { clock() - it } ?: 0L)
    fun quietMillis(): Long = activeMillis() - levelChangedAt

    /** Steps up the ladder; [requested] when the child asked for help. Returns false at the top. */
    fun stepUp(requested: Boolean): Boolean {
        if (requested) helpRequests++
        if (level >= Ladder.Model) return false
        level++
        maxLevel = maxOf(maxLevel, level)
        levelChangedAt = activeMillis()
        return true
    }

    /** A correct step of a multi-step trial (one seed of a sequence): counts as the first choice if none yet. */
    fun progress(choice: String, input: InputType) {
        if (solved) return
        if (firstChoice == null) { firstChoice = choice; firstCorrect = true; firstIndependent = level == Ladder.None }
        this.input = input
    }

    /** The child is working (touching, drawing), so the quiet spell before an automatic prompt starts again. */
    fun noteActivity() { if (!solved) levelChangedAt = activeMillis() }

    /** An input that hit no target, such as a missed drop or an obstacle touched. Not a wrong answer. */
    fun miss() { if (!solved) misses++ }

    /** Records a response. A correct one solves the trial; a wrong one steps the ladder up. Returns whether solved. */
    fun respond(choice: String, correct: Boolean, input: InputType): Boolean {
        if (solved) return true
        if (firstChoice == null) { firstChoice = choice; firstCorrect = correct; firstIndependent = level == Ladder.None }
        this.input = input
        if (correct) { solved = true; pause(); return true }
        wrongAttempts++
        stepUp(requested = false)
        return false
    }

    val outcome: String get() = if (firstCorrect && firstIndependent) "independent" else "with_help"

    fun toRecord(session: SessionRecord, itemKey: String, strokes: String?, detail: String? = null) = TrialRecord(
        id = id, sessionId = session.id, childId = session.childId, activity = session.activity, taskVersion = session.taskVersion,
        difficulty = session.difficulty, itemKey = itemKey, inputType = input.id, firstChoice = firstChoice, firstCorrect = firstCorrect,
        firstIndependent = firstIndependent, wrongAttempts = wrongAttempts, helpLevel = maxLevel, helpRequests = helpRequests,
        outcome = outcome, activeMillis = activeMillis(), startedAt = startedAt, endedAt = clock(),
        strokes = (attempts + listOfNotNull(strokes)).takeIf { it.isNotEmpty() }?.joinToString(StrokeCodec.AttemptBreak),
        misses = misses, detail = detail,
    )
}

/** Where sessions and trials are kept; Room in the app, a fake in tests. */
interface RecordStore {
    suspend fun insertSession(session: SessionRecord)
    suspend fun endSession(id: String, status: String, endedAt: Long)
    suspend fun activeSession(childId: Long, activity: String): SessionRecord?
    suspend fun insertTrial(trial: TrialRecord)
    suspend fun trialsIn(sessionId: String): List<TrialRecord>
    suspend fun completedSessions(childId: Long, activity: String): Int
}

class RoomRecordStore(private val dao: RecordsDao) : RecordStore {
    override suspend fun insertSession(session: SessionRecord) { dao.insertSession(session) }
    override suspend fun endSession(id: String, status: String, endedAt: Long) = dao.endSession(id, status, endedAt)
    override suspend fun activeSession(childId: Long, activity: String) = dao.activeSession(childId, activity)
    override suspend fun insertTrial(trial: TrialRecord) { dao.insertTrial(trial) }
    override suspend fun trialsIn(sessionId: String) = dao.trialsIn(sessionId)
    override suspend fun completedSessions(childId: Long, activity: String) = dao.completedCount(childId, activity)
}

/**
 * Runs one session: a token board of [ActivityPlan.slots] trials. Each solved trial is saved at once with an ID made
 * on the device when the trial started (FR-ACT-011, FR-SYN-002) and fills one slot (FR-RWD-001). A full board
 * completes the session; leaving early stops it, keeping every saved trial (FR-ACT-009). A session left "active" by
 * an interruption can be resumed with the same trials (FR-ACT-010). Each session's plan comes from [plans] with the
 * number of sessions this child has completed in the activity, so the level moves between sessions (FR-ACT-012).
 */
class SessionEngine(
    private val store: RecordStore,
    private val activity: String,
    private val plans: (completedSessions: Int) -> ActivityPlan,
    private val childId: Long,
    private val clock: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { UUID.randomUUID().toString() },
) {
    var session: SessionRecord? = null; private set
    var plan: ActivityPlan? = null; private set
    var trials: List<TrialSpec> = emptyList(); private set
    var index = 0; private set
    var tokens = 0; private set
    var tracker: TrialTracker? = null; private set
    private var lastLevel = Ladder.None

    val current: TrialSpec? get() = trials.getOrNull(index)
    val complete: Boolean get() = session?.let { tokens >= it.slots } ?: false

    /** An interrupted session for this child and activity, with how many trials it already saved. */
    suspend fun interrupted(): Pair<SessionRecord, Int>? =
        store.activeSession(childId, activity)?.let { it to store.trialsIn(it.id).size }

    suspend fun startNew(seed: Long = clock()) {
        store.activeSession(childId, activity)?.let { store.endSession(it.id, "stopped", clock()) }
        val plan = plans(store.completedSessions(childId, activity))
        val s = SessionRecord(newId(), childId, activity, plan.taskVersion, plan.difficulty, plan.slots, seed, "active", clock())
        store.insertSession(s)
        load(plan, s, done = 0, last = Ladder.None)
    }

    /** No session has completed since [s] began (starting another stops it), so the same count gives the same plan. */
    suspend fun resume(s: SessionRecord) {
        val done = store.trialsIn(s.id)
        load(plans(store.completedSessions(childId, activity)), s, done.size, done.lastOrNull()?.helpLevel ?: Ladder.None)
    }

    private fun load(p: ActivityPlan, s: SessionRecord, done: Int, last: Int) {
        plan = p; session = s; trials = p.trials(s.seed); index = done; tokens = done; lastLevel = last
        begin()
    }

    private fun begin() { tracker = if (index < trials.size && !complete) TrialTracker(newId(), Pending.fadedStart(lastLevel), clock) else null }

    /**
     * Records a response; when it solves the trial, saves it and fills a slot. Returns whether solved. A wrong drawing's
     * [strokes] are kept as an attempt; [detail] is saved with the solved trial.
     */
    suspend fun respond(choice: String, correct: Boolean, input: InputType, strokes: String? = null, detail: String? = null): Boolean {
        val t = tracker ?: return false
        val s = session ?: return false
        if (t.solved) return true
        if (!t.respond(choice, correct, input)) { strokes?.let { t.attempts += it }; return false }
        store.insertTrial(t.toRecord(s, current!!.key, strokes, detail))
        tokens++
        lastLevel = t.maxLevel
        return true
    }

    /** After a solved trial: the next trial, or completion when the board is full. */
    suspend fun advance() {
        val s = session ?: return
        index++
        if (complete) { store.endSession(s.id, "completed", clock()); tracker = null } else begin()
    }

    /** The child left before the board was full. Saved trials stay; the session is closed as stopped. */
    suspend fun stop() {
        val s = session ?: return
        if (!complete) store.endSession(s.id, "stopped", clock())
        session = null; tracker = null
    }
}
