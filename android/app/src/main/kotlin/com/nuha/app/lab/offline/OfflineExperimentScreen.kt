package com.nuha.app.lab.offline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nuha.app.lab.offline.data.SessionResult
import com.nuha.app.ui.components.NuhaMinTarget
import com.nuha.app.ui.components.NuhaPrimaryButton
import com.nuha.app.ui.components.NuhaSecondaryButton
import com.nuha.app.ui.components.TaskScaffold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Experiment 4: results survive app restarts and are never duplicated when synchronization resumes.
 * To test a restart: finish attempts, force-stop the app (or `adb shell am force-stop com.nuha.app`), reopen.
 */
@Composable
fun OfflineExperimentScreen(onBack: () -> Unit, vm: OfflineExperimentViewModel = viewModel()) {
    val results by vm.results.collectAsStateWithLifecycle()
    val remoteCount by vm.remoteCount.collectAsStateWithLifecycle()
    val ui by vm.ui.collectAsStateWithLifecycle()

    TaskScaffold(
        title = "تجربة 4: الحفظ دون اتصال",
        instruction = "أنهِ محاولة، ثم أعد تشغيل التطبيق أو شغّل الاتصال، وراقب العدد على الجهاز وعلى الخادم.",
        onBack = onBack,
        actions = {
            NuhaPrimaryButton("أنهِ المحاولة", onClick = { vm.finishAttempt() })
            NuhaSecondaryButton("محاولة جديدة", onClick = { vm.newAttempt() })
            NuhaSecondaryButton("زامن الآن", onClick = { vm.sync() })
            NuhaSecondaryButton("امسح بيانات التجربة", onClick = { vm.reset() })
        },
    ) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium, modifier = Modifier.width(380.dp).fillMaxHeight()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LabeledSwitch("متصل بالإنترنت (محاكاة)", ui.online, vm::setOnline)
                    LabeledSwitch("اقطع الاتصال بعد الرفع", ui.interruptAfterUpload, vm::setInterrupt)
                    Text("على الجهاز: ${results.size}", style = MaterialTheme.typography.titleLarge)
                    Text("بانتظار المزامنة: ${results.count { !it.synced }}", style = MaterialTheme.typography.titleMedium)
                    Text("على الخادم: $remoteCount", style = MaterialTheme.typography.titleLarge)
                    Text("المحاولة الحالية: ${ui.attemptId.take(8)}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium, modifier = Modifier.weight(1f).fillMaxHeight()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        ui.message,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                    // A results log is the one place scrolling is expected; the controls stay fixed.
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(results, key = { it.id }) { r -> ResultRow(r) }
                    }
                }
            }
        }
    }
}

private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

@Composable
private fun ResultRow(r: SessionResult) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(timeFormat.format(Date(r.finishedAtMillis)), style = MaterialTheme.typography.bodyMedium)
        Text("${r.activity}: ${r.correct} من ${r.total}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(if (r.synced) "✓ مُزامَن" else "⏳ على الجهاز فقط", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun LabeledSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = NuhaMinTarget), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
