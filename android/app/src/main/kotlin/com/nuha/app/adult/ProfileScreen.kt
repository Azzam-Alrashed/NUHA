package com.nuha.app.adult

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuha.app.Pending
import com.nuha.app.onboarding.AvatarNames
import com.nuha.app.ui.identity.AvatarKinds

private const val MAX_NAME = 20

/**
 * Edit a child's name, age and buddy, or delete the child with everything recorded for them after a confirmation
 * (FR-PRF-009). The name follows onboarding's rules: at most 20 characters, trimmed, never blank (FR-PRF-001).
 */
@Composable
fun ProfileScreen(model: ProfilesViewModel, childId: Long, onDone: () -> Unit) {
    val children by model.children.collectAsStateWithLifecycle()
    val child = children?.firstOrNull { it.id == childId }
    LaunchedEffect(children) { if (children != null && child == null) onDone() }
    BackHandler(onBack = onDone)
    if (child == null) return
    var name by rememberSaveable(childId) { mutableStateOf(child.name) }
    var age by rememberSaveable(childId) { mutableIntStateOf(child.age) }
    var avatar by rememberSaveable(childId) { mutableStateOf(child.avatar) }
    var confirming by rememberSaveable { mutableStateOf(false) }
    val error by model.error.collectAsStateWithLifecycle()
    AdultTheme {
        Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { AdultBar(AdultCopy.profileTitle(child.name), onDone) }) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(32.dp).widthIn(max = 760.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it.take(MAX_NAME) }, singleLine = true,
                    label = { Text(AdultCopy.Name) }, supportingText = { Text(AdultCopy.nameHelp(Pending.DefaultNumerals.format(MAX_NAME))) },
                    textStyle = MaterialTheme.typography.titleLarge, modifier = Modifier.widthIn(min = 420.dp),
                )
                Group(AdultCopy.Age) {
                    Options {
                        for (a in Pending.AgeRange) FilterChip(
                            selected = a == age, onClick = { age = a }, modifier = Modifier.heightIn(min = 52.dp),
                            label = { Text(Pending.DefaultNumerals.format(a), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 12.dp)) },
                        )
                    }
                }
                Group(AdultCopy.Avatar) {
                    Options { for (kind in AvatarKinds) AvatarOption(kind, kind == avatar, AvatarNames[kind] ?: kind) { avatar = kind } }
                }
                if (error) Text(AdultCopy.SaveError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { model.saveProfile(child.copy(name = name.trim(), age = age, avatar = avatar), onDone) },
                        enabled = name.isNotBlank(), modifier = Modifier.heightIn(min = 56.dp),
                    ) { Text(AdultCopy.Save, style = MaterialTheme.typography.labelLarge) }
                    OutlinedButton(
                        onClick = { confirming = true }, modifier = Modifier.heightIn(min = 56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) { Text(AdultCopy.DeleteChild, style = MaterialTheme.typography.labelLarge) }
                }
            }
        }
        if (confirming) AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text(AdultCopy.deleteTitle(child.name)) },
            text = { Text(AdultCopy.DeleteBody, style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(onClick = { confirming = false; model.delete(child.id, onDone) }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text(AdultCopy.Delete, style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = { TextButton(onClick = { confirming = false }) { Text(AdultCopy.Cancel, style = MaterialTheme.typography.labelLarge) } },
        )
    }
}
