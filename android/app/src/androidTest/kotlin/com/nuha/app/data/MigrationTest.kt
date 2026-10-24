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
 * IR-STO-002: every schema change keeps existing data. Builds a database exactly as version 1 shipped it (preferences
 * only), then opens it through the app's migrations and checks the saved preferences survive and new tables work.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val name = "migration-test.db"

    @Before @After fun clean() { context.deleteDatabase(name) }

    @Test fun version1KeepsPreferencesAndGainsChildProfiles() = runBlocking {
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name), null).use { db ->
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `app_preferences` (`id` INTEGER NOT NULL, `appearance` TEXT NOT NULL, `voice` INTEGER NOT NULL, " +
                    "`effects` INTEGER NOT NULL, `music` INTEGER NOT NULL, `reducedMotion` INTEGER NOT NULL, `onboardingComplete` INTEGER NOT NULL, PRIMARY KEY(`id`))",
            )
            db.execSQL("INSERT INTO app_preferences VALUES (1, 'muted', 1, 0, 0, 1, 1)")
            db.version = 1
        }
        val database = AppDatabase.build(context, name)
        try {
            val saved = database.preferences().read()
            assertNotNull(saved)
            assertEquals("muted", saved!!.appearance)
            assertEquals(false, saved.effects)
            assertEquals(true, saved.onboardingComplete)
            database.children().insert(ChildProfile(name = "Test", avatar = "dome", age = 4, answers = "nnnnnnnnnn", startPath = "mixed"))
            assertEquals("Test", database.children().first()!!.name)
        } finally {
            database.close()
        }
    }
}
