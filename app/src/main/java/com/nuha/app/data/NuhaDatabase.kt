package com.nuha.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * One finished activity, written on the device the moment it ends.
 * [id] is generated when the attempt starts, so saving or uploading it twice can never create two records.
 */
@Entity(tableName = "session_results")
data class SessionResult(
    @PrimaryKey val id: String,
    val activity: String,
    val correct: Int,
    val total: Int,
    val finishedAtMillis: Long,
    val synced: Boolean = false,
)

/** Stand-in for a server so the experiment can count what "arrived". No real backend exists yet. */
@Entity(tableName = "simulated_remote_results")
data class RemoteResult(
    @PrimaryKey val id: String,
    val receivedAtMillis: Long,
)

@Dao
interface ResultDao {
    /** IGNORE makes a repeated save of the same attempt a no-op. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(result: SessionResult): Long

    @Query("SELECT * FROM session_results ORDER BY finishedAtMillis DESC")
    fun observeAll(): Flow<List<SessionResult>>

    @Query("SELECT * FROM session_results WHERE synced = 0 ORDER BY finishedAtMillis")
    suspend fun unsynced(): List<SessionResult>

    @Query("UPDATE session_results SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Upsert
    suspend fun upsertRemote(result: RemoteResult)

    @Query("SELECT COUNT(*) FROM simulated_remote_results")
    fun observeRemoteCount(): Flow<Int>

    @Query("DELETE FROM session_results")
    suspend fun clearLocal()

    @Query("DELETE FROM simulated_remote_results")
    suspend fun clearRemote()
}

@Database(entities = [SessionResult::class, RemoteResult::class], version = 1, exportSchema = false)
abstract class NuhaDatabase : RoomDatabase() {
    abstract fun results(): ResultDao

    companion object {
        @Volatile private var instance: NuhaDatabase? = null

        fun get(context: Context): NuhaDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, NuhaDatabase::class.java, "nuha.db")
                .build()
                .also { instance = it }
        }
    }
}
