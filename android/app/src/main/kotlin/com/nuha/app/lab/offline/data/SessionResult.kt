package com.nuha.app.lab.offline.data

import androidx.room.Entity
import androidx.room.PrimaryKey

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
