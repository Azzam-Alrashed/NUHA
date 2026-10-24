package com.nuha.app.task

import com.nuha.app.Pending
import com.nuha.app.child.Activity
import com.nuha.app.data.SessionRecord
import com.nuha.app.data.TrialRecord
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Trial recording (FR-ACT-007, -010, -011; FR-RWD-001), against an in-memory store and a hand-driven clock. */
class EngineTest {
    private var now = 1_000L
    private val clock = { now }
    private var ids = 0
    private val newId = { "id-${++ids}" }

    private class FakeStore : RecordStore {
        val sessions = linkedMapOf<String, SessionRecord>()
        val trials = mutableListOf<TrialRecord>()
        override suspend fun insertSession(session: SessionRecord) { sessions.putIfAbsent(session.id, session) }
        override suspend fun endSession(id: String, status: String, endedAt: Long) { sessions[id] = sessions.getValue(id).copy(status = status, endedAt = endedAt) }
        override suspend fun activeSession(childId: Long, activity: String) = sessions.values.lastOrNull { it.childId == childId && it.activity == activity && it.status == "active" }
        override suspend fun insertTrial(trial: TrialRecord) { if (trials.none { it.id == trial.id }) trials += trial }
        override suspend fun trialsIn(sessionId: String) = trials.filter { it.sessionId == sessionId }
    }

    private object Plan : ActivityPlan {
        override val activity = Activity.Emotions
        override val taskVersion = 7
        override val difficulty = "level=1"
        override val slots = 3
        override fun trials(seed: Long) = List(slots) { i -> TrialSpec("item-$seed-$i", "ins", "praise", "model", i) }
    }

    @Test fun firstChoiceAndIndependence() {
        val t = TrialTracker("t", Ladder.None, clock)
        assertFalse(t.respond("b", correct = false, InputType.Finger))
        assertTrue(t.respond("a", correct = true, InputType.Stylus))
        assertEquals("b", t.firstChoice)
        assertFalse(t.firstCorrect)
        assertTrue(t.firstIndependent)
        assertEquals(1, t.wrongAttempts)
        assertEquals(Ladder.Repeat, t.maxLevel)
        assertEquals("with_help", t.outcome)
        assertEquals(InputType.Stylus, t.input)
    }

    @Test fun correctFirstTimeIsIndependent() {
        val t = TrialTracker("t", Ladder.None, clock)
        assertTrue(t.respond("a", correct = true, InputType.Finger))
        assertEquals("independent", t.outcome)
    }

    @Test fun promptedFirstAnswerIsNotIndependent() {
        val t = TrialTracker("t", Ladder.Hint, clock)
        t.respond("a", correct = true, InputType.Finger)
        assertFalse(t.firstIndependent)
        assertEquals("with_help", t.outcome)
    }

    @Test fun helpRequestsAndLadderTop() {
        val t = TrialTracker("t", Ladder.None, clock)
        repeat(5) { t.stepUp(requested = true) }
        assertEquals(5, t.helpRequests)
        assertEquals(Ladder.Model, t.level)
    }

    @Test fun activeTimeExcludesPauses() {
        val t = TrialTracker("t", Ladder.None, clock)
        now += 2_000; t.pause(); now += 60_000; t.resume(); now += 1_000
        assertEquals(3_000, t.activeMillis())
        assertEquals(3_000, t.quietMillis())
        t.stepUp(requested = false); now += 500
        assertEquals(500, t.quietMillis())
    }

    @Test fun helpFadesAcrossTrials() {
        assertEquals(0, Pending.fadedStart(Ladder.None))
        assertEquals(0, Pending.fadedStart(Ladder.Repeat))
        assertEquals(Ladder.Repeat, Pending.fadedStart(Ladder.Hint))
        assertEquals(Ladder.Hint, Pending.fadedStart(Ladder.Model))
    }

    @Test fun sessionFillsBoardSavesTrialsAndCompletes() = runBlocking {
        val store = FakeStore()
        val engine = SessionEngine(store, Plan, childId = 9, clock, newId)
        engine.startNew(seed = 42)
        val session = engine.session!!
        assertEquals("active", store.sessions.getValue(session.id).status)
        repeat(3) { i ->
            val trialId = engine.tracker!!.id
            now += 1_000
            assertFalse(engine.respond("wrong", correct = false, InputType.Finger))
            assertTrue(engine.respond("item", correct = true, InputType.Finger))
            // Saved at once, under the ID made when the trial started.
            assertEquals(trialId, store.trials.last().id)
            assertEquals(i + 1, engine.tokens)
            engine.advance()
        }
        assertTrue(engine.complete)
        assertNull(engine.tracker)
        assertEquals("completed", store.sessions.getValue(session.id).status)
        val t = store.trials.first()
        assertEquals(9, t.childId); assertEquals("emotions", t.activity); assertEquals(7, t.taskVersion); assertEquals("level=1", t.difficulty)
        assertEquals("item-42-0", t.itemKey); assertEquals("finger", t.inputType); assertEquals("with_help", t.outcome); assertEquals(1, t.wrongAttempts)
    }

    @Test fun savingTheSameTrialTwiceKeepsOne() = runBlocking {
        val store = FakeStore()
        val engine = SessionEngine(store, Plan, childId = 1, clock, newId)
        engine.startNew(seed = 1)
        engine.respond("a", correct = true, InputType.Finger)
        engine.respond("a", correct = true, InputType.Finger)
        assertEquals(1, store.trials.size)
        assertEquals(1, engine.tokens)
    }

    @Test fun stoppingKeepsTrialsAndClosesTheSession() = runBlocking {
        val store = FakeStore()
        val engine = SessionEngine(store, Plan, childId = 1, clock, newId)
        engine.startNew(seed = 1)
        val id = engine.session!!.id
        engine.respond("a", correct = true, InputType.Finger); engine.advance()
        engine.stop()
        assertEquals("stopped", store.sessions.getValue(id).status)
        assertEquals(1, store.trials.size)
        assertNull(engine.interrupted())
    }

    @Test fun interruptedSessionResumesWithTheSameTrials() = runBlocking {
        val store = FakeStore()
        val first = SessionEngine(store, Plan, childId = 1, clock, newId)
        first.startNew(seed = 5)
        first.respond("a", correct = true, InputType.Finger); first.advance()
        val nextKey = first.current!!.key
        // The app dies here: nothing else runs. A new engine finds the session and continues it.
        val second = SessionEngine(store, Plan, childId = 1, clock, newId)
        val (session, saved) = second.interrupted()!!
        assertEquals(1, saved)
        second.resume(session)
        assertEquals(1, second.tokens)
        assertEquals(nextKey, second.current!!.key)
    }

    @Test fun startingAfreshClosesTheInterruptedSession() = runBlocking {
        val store = FakeStore()
        val first = SessionEngine(store, Plan, childId = 1, clock, newId)
        first.startNew(seed = 5)
        val old = first.session!!.id
        val second = SessionEngine(store, Plan, childId = 1, clock, newId)
        second.startNew(seed = 6)
        assertEquals("stopped", store.sessions.getValue(old).status)
        assertEquals(0, second.tokens)
    }
}
