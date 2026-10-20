package com.nuha.app.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nuha.app.Routes

private data class Experiment(val route: String, val number: Int, val title: String, val question: String)

private val experiments = listOf(
    Experiment(Routes.TRACE, 1, "سطح الرسم بالقلم", "هل يتبع الحبر القلم بسلاسة؟ التراجع، والمسح، وإعادة العرض بعد تغيير الحجم."),
    Experiment(Routes.SELECT, 2, "لعبة يمكن الوصول إليها", "هل تصل عناصر اللعبة إلى قارئ الشاشة؟ النقر بديلًا عن السحب."),
    Experiment(Routes.LAYOUT, 3, "ملاءمة الشاشة", "هل يظهر النشاط كاملًا دون تمرير، حتى مع تكبير الخط؟"),
    Experiment(Routes.OFFLINE, 4, "الحفظ دون اتصال", "هل تبقى النتائج بعد إعادة التشغيل؟ وهل تتكرر عند المزامنة؟"),
)

/** Entry point for the pre-architecture technical experiments from the research brief. */
@Composable
fun LabHomeScreen(onOpen: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text("نُهى — مختبر التجارب التقنية", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        Text(
            "أربع تجارب قبل اعتماد البنية النهائية. النتائج تُسجَّل في docs/experiments.md.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            experiments.forEach { e ->
                Card(
                    onClick = { onOpen(e.route) },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("${e.number}", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                        Text(e.title, style = MaterialTheme.typography.titleLarge)
                        Text(e.question, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
