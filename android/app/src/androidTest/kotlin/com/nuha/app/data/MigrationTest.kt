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
 * IR-STO-002: every schema change keeps existing data. Builds databases exactly as versions 1 and 2 shipped them, opens
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

    private fun oldDatabase(version: Int, vararg sql: String) =
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name), null).use { db -> sql.forEach(db::execSQL); db.version = version }

    private fun trial(id: String) = TrialRecord(
        id = id, sessionId = "s1", childId = 1, activity = "emotions", taskVersion = 1, difficulty = "level=1", itemKey = "a", inputType = "finger",
        firstChoice = "a", firstCorrect = true, firstIndependent = true, wrongAttempts = 0, helpLevel = 0, helpRequests = 0, outcome = "independent",
        activeMillis = 1200, startedAt = 1, endedAt = 2,
    )

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
