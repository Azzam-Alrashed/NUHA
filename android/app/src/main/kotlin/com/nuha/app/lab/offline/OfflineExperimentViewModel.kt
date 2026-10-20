package com.nuha.app.lab.offline

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nuha.app.lab.offline.data.LabDatabase
import com.nuha.app.lab.offline.data.RemoteResult
import com.nuha.app.lab.offline.data.SessionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

data class OfflineUiState(
    val online: Boolean = false,
    val interruptAfterUpload: Boolean = false,
    val attemptId: String = UUID.randomUUID().toString(),
    val message: String = "",
)

class OfflineExperimentViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = LabDatabase.get(app).results()

    val results = dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val remoteCount = dao.observeRemoteCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _ui = MutableStateFlow(OfflineUiState())
    val ui = _ui.asStateFlow()

    /** Saves the current attempt locally. Pressing it again for the same attempt does nothing. */
    fun finishAttempt() = viewModelScope.launch {
        val total = 6
        val row = dao.insert(
            SessionResult(
                id = _ui.value.attemptId,
                activity = "رحلة مطابقة البيئة",
                correct = Random.nextInt(2, total + 1),
                total = total,
                finishedAtMillis = System.currentTimeMillis(),
            )
        )
        _ui.update { it.copy(message = if (row == -1L) "هذه المحاولة محفوظة مسبقًا — لم تُضف نسخة ثانية." else "حُفظت النتيجة على الجهاز.") }
    }

    fun newAttempt() = _ui.update { it.copy(attemptId = UUID.randomUUID().toString(), message = "بدأت محاولة جديدة.") }

    fun setOnline(online: Boolean) {
        _ui.update { it.copy(online = online) }
        if (online) sync()
    }

    fun setInterrupt(value: Boolean) = _ui.update { it.copy(interruptAfterUpload = value) }

    /**
     * Uploads unsynced results. Upload is an upsert keyed by the attempt id, so if the connection drops
     * after uploading but before marking as synced, the retry re-sends the same id and the server still
     * holds one copy.
     */
    fun sync() = viewModelScope.launch {
        if (!_ui.value.online) {
            _ui.update { it.copy(message = "لا يوجد اتصال — النتائج تبقى محفوظة على الجهاز.") }
            return@launch
        }
        val pending = dao.unsynced()
        for (r in pending) {
            dao.upsertRemote(RemoteResult(r.id, System.currentTimeMillis()))
            if (_ui.value.interruptAfterUpload) {
                _ui.update { it.copy(message = "انقطع الاتصال بعد الرفع وقبل التأكيد. أعد المزامنة للتحقق من عدم التكرار.") }
                return@launch
            }
            dao.markSynced(r.id)
        }
        _ui.update { it.copy(message = "اكتملت المزامنة: ${pending.size} نتيجة.") }
    }

    fun reset() = viewModelScope.launch {
        dao.clearLocal(); dao.clearRemote()
        _ui.update { OfflineUiState(online = it.online, interruptAfterUpload = it.interruptAfterUpload, message = "مُسحت بيانات التجربة.") }
    }
}
