package com.nuha.app.adult

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * Before a new child's first session the adult picks a sensory starting point, lively, gentle or muted (FR-SET-005);
 * saving is not possible without a choice. What each point sets is PLACEHOLDER OI-14 ([com.nuha.app.Pending.sensoryPreset]).
 */
@Composable
fun SensoryScreen(saving: Boolean, error: Boolean, onBack: () -> Unit, onSave: (String) -> Unit) {
    var choice by rememberSaveable { mutableStateOf<String?>(null) }
    BackHandler(onBack = onBack)
    AdultTheme {
        Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { AdultBar(AdultCopy.Sensory, onBack) }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(32.dp), verticalArrangement = Arrangement.spacedBy(28.dp)) {
                Text(AdultCopy.SensoryPrompt, style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    for ((id, title, body) in AdultCopy.SensoryChoices) {
                        val selected = choice == id
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(if (selected) 3.dp else 1.5.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.width(300.dp).heightIn(min = 160.dp).selectable(selected, role = Role.RadioButton) { choice = id },
                        ) {
                            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(if (selected) "✓ $title" else title, style = MaterialTheme.typography.titleLarge)
                                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                if (error) Text(AdultCopy.SaveError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                Button(onClick = { choice?.let(onSave) }, enabled = choice != null && !saving, modifier = Modifier.heightIn(min = 56.dp)) {
                    Text(AdultCopy.SaveChild, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
