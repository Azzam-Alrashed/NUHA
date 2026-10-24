package com.nuha.app.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Device preferences; child profiles are their own table below, and the disposable experiment database stays separate. */
@Entity(tableName = "app_preferences")
data class AppPreferences(
    @PrimaryKey val id: Int = 1,
    val appearance: String = "colourful",
    val voice: Boolean = true,
    val effects: Boolean = true,
    val music: Boolean = false,
    val reducedMotion: Boolean = true,
    val onboardingComplete: Boolean = false,
)

@Dao
interface PreferencesDao {
    @Query("SELECT * FROM app_preferences WHERE id = 1")
    suspend fun read(): AppPreferences?
    @Upsert suspend fun save(preferences: AppPreferences)
}

/**
 * A child set up during onboarding. [answers] holds one letter per observation question (y, n or u for
 * unsure), and [startPath] is where the road begins for them; both are a starting suggestion, never a diagnosis.
 */
@Entity(tableName = "child_profiles")
data class ChildProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatar: String,
    val age: Int,
    val answers: String,
    val startPath: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Dao
interface ChildDao {
    @Insert suspend fun insert(child: ChildProfile): Long
    @Update suspend fun update(child: ChildProfile)
    @Query("SELECT * FROM child_profiles ORDER BY createdAt LIMIT 1")
    suspend fun first(): ChildProfile?
    @Query("SELECT * FROM child_profiles WHERE id = :id")
    suspend fun byId(id: Long): ChildProfile?
    @Query("SELECT * FROM child_profiles ORDER BY createdAt")
    fun observeAll(): Flow<List<ChildProfile>>
    @Query("DELETE FROM child_profiles WHERE id = :id")
    suspend fun delete(id: Long)
}

@Database(entities = [AppPreferences::class, ChildProfile::class, SessionRecord::class, TrialRecord::class, ChildSettings::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun preferences(): PreferencesDao
    abstract fun children(): ChildDao
    abstract fun records(): RecordsDao
    abstract fun settings(): SettingsDao

    /** Saves a new child and their settings, all or nothing; returns the child's id. */
    suspend fun addChild(child: ChildProfile, settingsFor: (Long) -> ChildSettings): Long = withTransaction {
        val id = children().insert(child)
        settings().save(settingsFor(id))
        id
    }

    /** Deletes a child with their settings and every session and trial recorded for them, all or nothing (FR-PRF-009). */
    suspend fun deleteChild(id: Long) = withTransaction {
        records().deleteTrials(id)
        records().deleteSessions(id)
        settings().delete(id)
        children().delete(id)
    }
    companion object {
        @Volatile private var instance: AppDatabase? = null
        private val AddChildProfiles = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `child_profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
                        "`avatar` TEXT NOT NULL, `age` INTEGER NOT NULL, `answers` TEXT NOT NULL, `startPath` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)",
                )
            }
        }
        private val AddActivityRecords = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) { RecordsSchema.create.forEach(db::execSQL) }
        }
        private val AddTrialDetail = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) { RecordsSchema.addMissesAndDetail.forEach(db::execSQL) }
        }
        private val AddChildSettings = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) { SettingsSchema.create.forEach(db::execSQL) }
        }
        /** Every migration, oldest first; tests open old databases through these. */
        val Migrations = arrayOf(AddChildProfiles, AddActivityRecords, AddTrialDetail, AddChildSettings)
        fun build(context: android.content.Context, name: String): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, name).addMigrations(*Migrations).build()
        fun get(application: Application): AppDatabase = instance ?: synchronized(this) {
            instance ?: build(application, "nuha-app.db").also { instance = it }
        }
    }
}

class PreferencesViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.get(application).preferences()
    private val mutable = MutableStateFlow<AppPreferences?>(null)
    val preferences = mutable.asStateFlow()
    val error = MutableStateFlow(false)
    val finishing = MutableStateFlow(false)
    // One writer preserves user action order, including rapid changes followed immediately by Finish.
    private data class Write(val value: AppPreferences, val onSaved: (() -> Unit)? = null)
    private val writes = Channel<Write>(Channel.UNLIMITED)
    init {
        viewModelScope.launch {
            try { mutable.value = dao.read() ?: AppPreferences() }
            catch (_: Exception) { error.value = true }
        }
        viewModelScope.launch {
            for (write in writes) {
                val value = write.value
                try {
                    dao.save(value)
                    error.value = false
                    if (write.onSaved != null) {
                        mutable.value = value
                        finishing.value = false
                    }
                    write.onSaved?.invoke()
                } catch (_: Exception) {
                    error.value = true
                    finishing.value = false
                }
            }
        }
    }
    fun update(change: (AppPreferences) -> AppPreferences) {
        if (finishing.value) return
        val next = change(mutable.value ?: return)
        mutable.value = next
        writes.trySend(Write(next))
    }
    fun finish(onSaved: () -> Unit = {}) {
        if (finishing.value) return
        finishing.value = true
        writes.trySend(Write((mutable.value ?: return).copy(onboardingComplete = true), onSaved))
    }
}
