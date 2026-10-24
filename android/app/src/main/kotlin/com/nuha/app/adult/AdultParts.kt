package com.nuha.app.adult

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nuha.app.ui.identity.CheckBadge
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.NuhaAvatar
import com.nuha.app.ui.identity.avatarHue

/** A top bar with a title and a «رجوع» button at its start (the right, in RTL). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AdultBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        navigationIcon = { TextButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) { Text("→ ${AdultCopy.Back}", style = MaterialTheme.typography.titleMedium) } },
    )
}

/** A titled group of settings. */
@Composable
internal fun Group(title: String, note: String? = null, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
        note?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        content()
    }
}

/** One choice among a few, as a segmented row; [options] are (value, label). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Choice(title: String, options: List<Pair<String, String>>, value: String, note: String? = null, onChange: (String) -> Unit) {
    Group(title, note) {
        SingleChoiceSegmentedButtonRow(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            options.forEachIndexed { i, (key, label) ->
                SegmentedButton(
                    selected = key == value, onClick = { if (key != value) onChange(key) },
                    shape = SegmentedButtonDefaults.itemShape(i, options.size), modifier = Modifier.heightIn(min = 52.dp),
                ) { Text(label, style = MaterialTheme.typography.labelLarge) }
            }
        }
    }
}

/** A buddy to pick, with the identity's selected cues: tint face, thicker edge and a check badge (FR-PRF-006). */
@Composable
internal fun AvatarOption(kind: String, selected: Boolean, description: String, onSelect: () -> Unit) {
    val hue = LocalIdentity.current.avatarHue(kind)
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) hue.tint else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (selected) 4.dp else 1.5.dp, if (selected) hue.lip else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.size(104.dp).selectable(selected, role = Role.RadioButton, onClick = onSelect).semantics { this.contentDescription = description },
    ) {
        Box(contentAlignment = Alignment.Center) {
            NuhaAvatar(kind, Modifier.size(72.dp))
            if (selected) CheckBadge(hue, Modifier.align(AbsoluteAlignment.TopRight).padding(6.dp))
        }
    }
}

/** A row of options with even gaps. */
@Composable
internal fun Options(content: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) { content() }
}
