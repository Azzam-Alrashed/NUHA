package com.nuha.app.data

import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * IR-STO-002: every schema change keeps existing data. Builds databases exactly as versions 1, 2 and 3 shipped them, opens
 * them through the app's migrations, and checks saved data survives and the new tables work. IR-STO-003: a trial saved
 * twice under one ID is kept once.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val name = "migration-test.db"

    @Before @After fun clean() { context.deleteDatabase(name) }

    private val v1Preferences =
        "CREATE TABLE IF NOT EXISTS `app_preferences` (`id` INTEGER NOT NULL, `appearance` TEXT NOT NULL, `voice` INTEGER NOT NULL, " +
            "`effects` INTEGER NOT NULL, `music` INTEGER NOT NULL, `reducedMotion` INTEGER NOT NULL, `onboardingComplete` INTEGER NOT NULL, PRIMARY KEY(`id`))"
    private val v2Children =
        "CREATE TABLE IF NOT EXISTS `child_profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
            "`avatar` TEXT NOT NULL, `age` INTEGER NOT NULL, `answers` TEXT NOT NULL, `startPath` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)"

    private val v3Records = arrayOf(
        "CREATE TABLE IF NOT EXISTS `sessions` (`id` TEXT NOT NULL, `childId` INTEGER NOT NULL, `activity` TEXT NOT NULL, `taskVersion` INTEGER NOT NULL, " +
            "`difficulty` TEXT NOT NULL, `slots` INTEGER NOT NULL, `seed` INTEGER NOT NULL, `status` TEXT NOT NULL, `startedAt` INTEGER NOT NULL, `endedAt` INTEGER, PRIMARY KEY(`id`))",
        "CREATE INDEX IF NOT EXISTS `index_sessions_childId_activity` ON `sessions` (`childId`, `activity`)",
        "CREATE TABLE IF NOT EXISTS `trials` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `childId` INTEGER NOT NULL, `activity` TEXT NOT NULL, " +
            "`taskVersion` INTEGER NOT NULL, `difficulty` TEXT NOT NULL, `itemKey` TEXT NOT NULL, `inputType` TEXT NOT NULL, `firstChoice` TEXT, " +
            "`firstCorrect` INTEGER NOT NULL, `firstIndependent` INTEGER NOT NULL, `wrongAttempts` INTEGER NOT NULL, `helpLevel` INTEGER NOT NULL, " +
            "`helpRequests` INTEGER NOT NULL, `outcome` TEXT NOT NULL, `activeMillis` INTEGER NOT NULL, `startedAt` INTEGER NOT NULL, `endedAt` INTEGER NOT NULL, " +
            "`strokes` TEXT, PRIMARY KEY(`id`))",
        "CREATE INDEX IF NOT EXISTS `index_trials_childId_activity` ON `trials` (`childId`, `activity`)",
    )

    private fun oldDatabase(version: Int, vararg sql: String) =
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name), null).use { db -> sql.forEach(db::execSQL); db.version = version }

    private fun trial(id: String) = TrialRecord(
        id = id, sessionId = "s1", childId = 1, activity = "emotions", taskVersion = 1, difficulty = "level=1", itemKey = "a", inputType = "finger",
        firstChoice = "a", firstCorrect = true, firstIndependent = true, wrongAttempts = 0, helpLevel = 0, helpRequests = 0, outcome = "independent",
        activeMillis = 1200, startedAt = 1, endedAt = 2,
    )

    @Test fun version3KeepsTrialsAndGainsMissesAndDetail() = runBlocking {
        oldDatabase(
            3, v1Preferences, v2Children, *v3Records,
            "INSERT INTO app_preferences VALUES (1, 'colourful', 1, 1, 0, 0, 1)",
            "INSERT INTO child_profiles (name, avatar, age, answers, startPath, createdAt) VALUES ('Sara', 'bean', 5, 'nnnyynnnyy', 'attention', 10)",
            "INSERT INTO sessions VALUES ('s1', 1, 'star', 1, 'level=0', 5, 42, 'completed', 1, 9)",
            "INSERT INTO trials VALUES ('t1', 's1', 1, 'star', 1, 'level=0', 'run-0-arch', 'stylus', 'run', 1, 1, 0, 0, 0, 'independent', 8000, 1, 2, 'P|0.1,0.2,0')",
        )
        val database = AppDatabase.build(context, name)
        try {
            val records = database.records()
            val old = records.trialsIn("s1").single()
            assertEquals("run-0-arch", old.itemKey)
            assertEquals("P|0.1,0.2,0", old.strokes)
            assertEquals(0, old.misses)
            assertEquals(null, old.detail)
            assertEquals(1, records.completedCount(1, "star"))
            records.insertTrial(trial("t2").copy(sessionId = "s1", misses = 2, detail = "contacts=2;restarts=1"))
            val added = records.trialsIn("s1").first { it.id == "t2" }
            assertEquals(2, added.misses)
            assertEquals("contacts=2;restarts=1", added.detail)
        } finally {
            database.close()
        }
    }

    @Test fun version2KeepsChildrenAndGainsRecords() = runBlocking {
        oldDatabase(
            2, v1Preferences, v2Children,
            "INSERT INTO app_preferences VALUES (1, 'contrast', 1, 1, 0, 0, 1)",
            "INSERT INTO child_profiles (name, avatar, age, answers, startPath, createdAt) VALUES ('Sara', 'bean', 5, 'nnnyynnnyy', 'attention', 10)",
        )
        val database = AppDatabase.build(context, name)
        try {
            assertEquals("contrast", database.preferences().read()!!.appearance)
            assertEquals("Sara", database.children().first()!!.name)
            val records = database.records()
            records.insertSession(SessionRecord("s1", 1, "emotions", 1, "level=1", 5, 42, "active", 1))
            records.insertTrial(trial("t1"))
            records.insertTrial(trial("t1").copy(outcome = "with_help"))
            val saved = records.trialsIn("s1")
            assertEquals(1, saved.size)
            assertEquals("independent", saved.single().outcome)
            assertEquals("s1", records.activeSession(1, "emotions")!!.id)
        } finally {
            database.close()
        }
    }

    @Test fun version1KeepsPreferencesAndGainsChildProfiles() = runBlocking {
        oldDatabase(1, v1Preferences, "INSERT INTO app_preferences VALUES (1, 'muted', 1, 0, 0, 1, 1)")
        val database = AppDatabase.build(context, name)
        try {
            val saved = database.preferences().read()
            assertNotNull(saved)
            assertEquals("muted", saved!!.appearance)
            assertEquals(false, saved.effects)
            assertEquals(true, saved.onboardingComplete)
            database.children().insert(ChildProfile(name = "Test", avatar = "dome", age = 4, answers = "nnnnnnnnnn", startPath = "mixed"))
            assertEquals("Test", database.children().first()!!.name)
            assertEquals(0, database.records().completedByActivity(1).size)
        } finally {
            database.close()
        }
    }
}
