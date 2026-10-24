package com.nuha.app.adult

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nuha.app.data.AppDatabase
import com.nuha.app.data.ChildProfile
import com.nuha.app.data.ChildSettings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The children on this device and their settings, for the adult area and for choosing who plays (FR-PRF-008/009). */
class ProfilesViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.get(application)

    /** Every child, oldest first; null until loaded. */
    val children: StateFlow<List<ChildProfile>?> = database.children().observeAll().stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val error = MutableStateFlow(false)

    fun settings(childId: Long): Flow<ChildSettings> = database.settings().observe(childId).map { it ?: ChildSettings(childId) }

    // One writer applies every change in the order it was made (FR-SET-003), so rapid toggles end on the last value.
    private val writes = Channel<suspend () -> Unit>(Channel.UNLIMITED)
    init {
        viewModelScope.launch {
            for (write in writes) {
                try { write(); error.value = false } catch (_: Exception) { error.value = true }
            }
        }
    }

    fun saveSettings(settings: ChildSettings) { writes.trySend { database.settings().save(settings) } }
    fun saveProfile(child: ChildProfile, onSaved: () -> Unit) { writes.trySend { database.children().update(child); onSaved() } }
    fun delete(childId: Long, onDeleted: () -> Unit) { writes.trySend { database.deleteChild(childId); onDeleted() } }
}
