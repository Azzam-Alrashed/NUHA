package com.nuha.app.adult

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuha.app.Pending
import com.nuha.app.data.ChildProfile
import com.nuha.app.ui.identity.NuhaAvatar

/**
 * The adult area's home (FR-ADM-001): every child with one-tap play (FR-ADM-006), their profile and their settings,
 * and adding a child through onboarding without touching the others (FR-ONB-012). Reports arrive in milestone 5.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdultHome(model: ProfilesViewModel, onPlay: (Long) -> Unit, onProfile: (Long) -> Unit, onSettings: (Long) -> Unit, onAdd: () -> Unit, onExit: () -> Unit) {
    val children by model.children.collectAsStateWithLifecycle()
    BackHandler(onBack = onExit)
    AdultTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text(AdultCopy.Home, style = MaterialTheme.typography.headlineSmall) },
                    actions = { TextButton(onClick = onExit, modifier = Modifier.padding(end = 16.dp)) { Text(AdultCopy.BackToPlay, style = MaterialTheme.typography.titleMedium) } },
                )
            },
        ) { padding ->
            LazyVerticalGrid(
                GridCells.Adaptive(380.dp),
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(32.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(AdultCopy.Children, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                }
                items(children.orEmpty(), key = { it.id }) { child ->
                    ChildCard(child, onPlay = { onPlay(child.id) }, onProfile = { onProfile(child.id) }, onSettings = { onSettings(child.id) })
                }
                item {
                    OutlinedCard(onClick = onAdd, modifier = Modifier.fillMaxWidth().heightIn(min = 196.dp)) {
                        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("+", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                            Text(AdultCopy.AddChild, style = MaterialTheme.typography.titleLarge)
                            Text(AdultCopy.AddChildNote, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChildCard(child: ChildProfile, onPlay: () -> Unit, onProfile: () -> Unit, onSettings: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                NuhaAvatar(child.avatar, Modifier.size(84.dp))
                Column {
                    Text(child.name, style = MaterialTheme.typography.headlineSmall)
                    Text(AdultCopy.age(Pending.DefaultNumerals.format(child.age)), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onPlay, modifier = Modifier.heightIn(min = 52.dp)) { Text(AdultCopy.Play, style = MaterialTheme.typography.labelLarge) }
                OutlinedButton(onClick = onProfile, modifier = Modifier.heightIn(min = 52.dp)) { Text(AdultCopy.Profile, style = MaterialTheme.typography.labelLarge) }
                OutlinedButton(onClick = onSettings, modifier = Modifier.heightIn(min = 52.dp)) { Text(AdultCopy.Settings, style = MaterialTheme.typography.labelLarge) }
            }
        }
    }
}
