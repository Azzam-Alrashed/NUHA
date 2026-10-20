package com.nuha.app.lab.offline.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

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
