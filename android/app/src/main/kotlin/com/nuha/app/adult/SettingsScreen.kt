package com.nuha.app.adult

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuha.app.Numerals
import com.nuha.app.Pending
import com.nuha.app.child.Activity
import com.nuha.app.data.ChildSettings

/**
 * One child's settings (FR-SET-002): appearance, motion, voice, effects, music, celebration, mascot, prompt delay,
 * target size, numerals and map lock. Each change shows at once and is saved in order (FR-SET-003); child mode reads
 * the saved values, so it changes for this child only.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(model: ProfilesViewModel, childId: Long, onDone: () -> Unit) {
    val children by model.children.collectAsStateWithLifecycle()
    val child = children?.firstOrNull { it.id == childId }
    val saved by remember(childId) { model.settings(childId) }.collectAsStateWithLifecycle(initialValue = null)
    // Edits apply to this copy first, so rapid changes never read back a stale value from the database.
    var local by remember(childId) { mutableStateOf<ChildSettings?>(null) }
    LaunchedEffect(saved) { if (local == null) local = saved }
    BackHandler(onBack = onDone)
    val s = local
    if (child == null || s == null) return
    fun change(next: ChildSettings) { local = next; model.saveSettings(next) }
    val numerals = Pending.DefaultNumerals
    AdultTheme {
        Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { AdultBar(AdultCopy.settingsTitle(child.name), onDone) }) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 32.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                Text(AdultCopy.AppliesNote, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Choice(AdultCopy.Appearance, AdultCopy.AppearanceChoices, s.appearance) { change(s.copy(appearance = it)) }
                Choice(AdultCopy.Motion, AdultCopy.MotionChoices, s.motion) { change(s.copy(motion = it)) }
                Group(AdultCopy.Sound) {
                    Column(Modifier.widthIn(max = 720.dp)) {
                        Toggle(AdultCopy.Voice, null, s.voice) { change(s.copy(voice = it)) }
                        Toggle(AdultCopy.Effects, null, s.effects) { change(s.copy(effects = it)) }
                        Toggle(AdultCopy.Music, AdultCopy.MusicNote, s.music) { change(s.copy(music = it)) }
                    }
                }
                Choice(AdultCopy.Celebration, AdultCopy.CelebrationChoices, s.celebration) { change(s.copy(celebration = it)) }
                Choice(AdultCopy.Mascot, AdultCopy.MascotChoices, s.mascot) { change(s.copy(mascot = it)) }
                Choice(
                    AdultCopy.PromptDelay,
                    Pending.PromptDelayChoices.map { it.toString() to if (it == 0) AdultCopy.Never else AdultCopy.seconds(numerals.format(it)) },
                    s.promptDelaySeconds.toString(),
                ) { change(s.copy(promptDelaySeconds = it.toInt())) }
                Choice(AdultCopy.TargetSize, AdultCopy.TargetChoices, s.targetSize) { change(s.copy(targetSize = it)) }
                // Per child until OI-02 settles one numeral system. The sample counts left to right, as numbers do; the marks keep
                // Arabic-Indic digits and their spaces from turning into one right-to-left run.
                Choice(AdultCopy.Numerals, Numerals.entries.map { n -> n.id to "\u2066" + (1..3).joinToString(" ") { n.format(it) + "\u200E" } + "\u2069" }, s.numerals) { change(s.copy(numerals = it)) }
                Group(AdultCopy.MapLock, AdultCopy.MapLockNote) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(selected = s.mapLock == null, onClick = { change(s.copy(mapLock = null)) }, label = { Text(AdultCopy.WholeMap, style = MaterialTheme.typography.labelLarge) }, modifier = Modifier.heightIn(min = 48.dp))
                        for (a in Activity.entries) FilterChip(
                            selected = s.mapLock == a.id, onClick = { change(s.copy(mapLock = a.id)) },
                            label = { Text(a.label, style = MaterialTheme.typography.labelLarge) }, modifier = Modifier.heightIn(min = 48.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Toggle(label: String, note: String?, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).toggleable(value, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            note?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Switch(checked = value, onCheckedChange = null)
    }
}
