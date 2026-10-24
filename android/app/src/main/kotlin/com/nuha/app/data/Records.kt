package com.nuha.app.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/**
 * One run of an activity for one child, from opening it until the token board is full or the child stops.
 * [id] is created on the device when the session starts. [seed] regenerates the same trials on resume.
 * [status]: "active" (running or interrupted), "completed" (token board full) or "stopped" (the child left).
 */
@Entity(tableName = "sessions", indices = [Index("childId", "activity")])
data class SessionRecord(
    @PrimaryKey val id: String,
    val childId: Long,
    val activity: String,
    val taskVersion: Int,
    val difficulty: String,
    val slots: Int,
    val seed: Long,
    val status: String,
    val startedAt: Long,
    val endedAt: Long? = null,
)

/**
 * One trial (FR-ACT-011), written when it ends (FR-SYN-002). [id] is created on the device when the trial starts,
 * so saving twice, or a later upload retried, never duplicates it (IR-STO-003, EXP §4).
 * [firstIndependent]: the first response came before any prompt. [helpLevel]: the highest prompt-ladder level reached.
 * [outcome]: "independent" (first response correct and unprompted) or "with_help". [strokes]: drawing trials only.
 */
@Entity(tableName = "trials", indices = [Index("childId", "activity")])
data class TrialRecord(
    @PrimaryKey val id: String,
    val sessionId: String,
    val childId: Long,
    val activity: String,
    val taskVersion: Int,
    val difficulty: String,
    val itemKey: String,
    val inputType: String,
    val firstChoice: String?,
    val firstCorrect: Boolean,
    val firstIndependent: Boolean,
    val wrongAttempts: Int,
    val helpLevel: Int,
    val helpRequests: Int,
    val outcome: String,
    val activeMillis: Long,
    val startedAt: Long,
    val endedAt: Long,
    val strokes: String? = null,
)

@Dao
interface RecordsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertSession(session: SessionRecord): Long
    @Query("UPDATE sessions SET status = :status, endedAt = :endedAt WHERE id = :id")
    suspend fun endSession(id: String, status: String, endedAt: Long)
    @Query("SELECT * FROM sessions WHERE childId = :childId AND activity = :activity AND status = 'active' ORDER BY startedAt DESC LIMIT 1")
    suspend fun activeSession(childId: Long, activity: String): SessionRecord?
    /** Saving the same trial twice keeps the first copy: the insert is ignored, never duplicated. */
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertTrial(trial: TrialRecord): Long
    @Query("SELECT * FROM trials WHERE sessionId = :sessionId ORDER BY startedAt")
    suspend fun trialsIn(sessionId: String): List<TrialRecord>
    @Query("SELECT activity, COUNT(*) AS completed FROM sessions WHERE childId = :childId AND status = 'completed' GROUP BY activity")
    suspend fun completedByActivity(childId: Long): List<ActivityCount>
}

data class ActivityCount(val activity: String, val completed: Int)

/** Schema v3 SQL, kept beside the entities it must match exactly (Room validates it after migrating). */
internal object RecordsSchema {
    val create = listOf(
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
}
