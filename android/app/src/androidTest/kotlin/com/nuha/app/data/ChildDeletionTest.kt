package com.nuha.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/** FR-PRF-009: deleting a child removes them, their settings and every record, and leaves other children alone. */
@RunWith(AndroidJUnit4::class)
class ChildDeletionTest {
    private val database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()

    @After fun close() = database.close()

    private fun profile(name: String) = ChildProfile(name = name, avatar = "dome", age = 4, answers = "nnnnnnnnnn", startPath = "mixed")
    private fun session(id: String, child: Long) = SessionRecord(id, child, "garden", 1, "level=0", 5, 1, "completed", 1, 2)
    private fun trial(id: String, session: String, child: Long) = TrialRecord(
        id = id, sessionId = session, childId = child, activity = "garden", taskVersion = 1, difficulty = "level=0", itemKey = "garden-0", inputType = "finger",
        firstChoice = "seed-1", firstCorrect = true, firstIndependent = true, wrongAttempts = 0, helpLevel = 0, helpRequests = 0, outcome = "independent",
        activeMillis = 900, startedAt = 1, endedAt = 2,
    )

    @Test fun deletingOneChildRemovesOnlyTheirData() = runBlocking {
        val sara = database.addChild(profile("Sara")) { ChildSettings(it, appearance = "contrast") }
        val omar = database.addChild(profile("Omar")) { ChildSettings(it, appearance = "muted") }
        val records = database.records()
        records.insertSession(session("s1", sara)); records.insertTrial(trial("t1", "s1", sara))
        records.insertSession(session("s2", omar)); records.insertTrial(trial("t2", "s2", omar)); records.insertTrial(trial("t3", "s2", omar))

        database.deleteChild(omar)

        assertNull(database.children().byId(omar))
        assertNull(database.settings().get(omar))
        assertEquals(0, records.trialsIn("s2").size)
        assertEquals(0, records.completedCount(omar, "garden"))
        assertEquals("Sara", database.children().byId(sara)!!.name)
        assertEquals("contrast", database.settings().get(sara)!!.appearance)
        assertEquals(1, records.trialsIn("s1").size)
        assertEquals(1, records.completedCount(sara, "garden"))
    }
}
