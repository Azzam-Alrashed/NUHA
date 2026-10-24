package com.nuha.app.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Device preferences only; child profiles and the disposable experiment database remain separate. */
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

@Database(entities = [AppPreferences::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun preferences(): PreferencesDao
    companion object {
        @Volatile private var instance: AppDatabase? = null
        fun get(application: Application): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(application, AppDatabase::class.java, "nuha-app.db")
                .build().also { instance = it }
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
