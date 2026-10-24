package com.nuha.app.onboarding

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nuha.app.Pending
import com.nuha.app.data.AppDatabase
import com.nuha.app.data.ChildProfile
import com.nuha.app.ui.theme.systemAnimationsOff
import kotlinx.coroutines.launch

/** The child being set up across onboarding steps 2 and 3; saved only when the grown-up finishes. */
class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.get(application)

    var avatar by mutableStateOf<String?>(null)
    var name by mutableStateOf("")
    var age by mutableIntStateOf(Pending.DefaultAge)
    val answers = mutableStateListOf<Answer?>().apply { repeat(Questions.size) { add(null) } }
    /** Index of the question on screen; [Questions].size means the starting-path result. */
    var question by mutableIntStateOf(0)
    var saving by mutableStateOf(false)
        private set
    var error by mutableStateOf(false)
        private set

    val childReady get() = avatar != null && name.isNotBlank()
    val path get() = startPathFor(answers)

    /** Saves the child with the settings of [sensory], a sensory starting point (FR-SET-005); [onSaved] gets their id. */
    fun save(sensory: String, onSaved: (Long) -> Unit) {
        if (saving) return
        saving = true
        error = false
        viewModelScope.launch {
            try {
                val profile = ChildProfile(
                    name = name.trim(), avatar = avatar ?: return@launch, age = age,
                    answers = answers.joinToString("") { (it?.code ?: '-').toString() }, startPath = path.id,
                )
                val motionOff = systemAnimationsOff(getApplication())
                val id = database.addChild(profile) { Pending.sensoryPreset(sensory, it, motionOff) }
                onSaved(id)
            } catch (_: Exception) {
                error = true
                saving = false
            }
        }
    }
}
