package com.nuha.app.lab.offline.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Stand-in for a server so the experiment can count what "arrived". No real backend exists yet. */
@Entity(tableName = "simulated_remote_results")
data class RemoteResult(
    @PrimaryKey val id: String,
    val receivedAtMillis: Long,
)
