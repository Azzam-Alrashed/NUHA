package com.nuha.app.lab.select

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuha.app.ui.components.NuhaMinTarget
import com.nuha.app.ui.components.NuhaSecondaryButton
import com.nuha.app.ui.components.TaskScaffold

private enum class Place(val label: String) { RAIN("المطر"), SUN("الشمس") }

private data class Item(val id: Int, val emoji: String, val name: String, val belongs: Place)

private val items = listOf(
    Item(1, "☂️", "مظلة", Place.RAIN),
    Item(2, "🕶️", "نظارة شمسية", Place.SUN),
    Item(3, "🥾", "حذاء مطر", Place.RAIN),
    Item(4, "👒", "قبعة شمس", Place.SUN),
)

/** What the task records: conceptual choices, kept separate from missed taps (a motor issue, not a concept error). */
private data class Record(val firstChoiceCorrect: Int = 0, val firstChoiceWrong: Int = 0, val retries: Int = 0)

/**
 * Experiment 2: a matching task whose game elements are exposed to accessibility services.
 * Input is tap-item then tap-destination (no dragging), and TalkBack users get a one-step custom action.
 */
@Composable
fun SelectionExperimentScreen(onBack: () -> Unit) {
    val placed = remember { mutableStateMapOf<Int, Place>() }
    val attempted = remember { mutableStateMapOf<Int, Boolean>() }
    var selectedId by remember { mutableStateOf<Int?>(null) }
    var feedback by remember { mutableStateOf("اختر شيئًا، ثم اختر مكانه.") }
    var record by remember { mutableStateOf(Record()) }

    fun place(item: Item, target: Place) {
        val firstTry = attempted[item.id] != true
        attempted[item.id] = true
        if (item.belongs == target) {
            placed[item.id] = target
            selectedId = null
            feedback = "أحسنت! ${item.name} مع ${target.label}."
            record = if (firstTry) record.copy(firstChoiceCorrect = record.firstChoiceCorrect + 1) else record
        } else {
            feedback = "${item.name} لا تناسب ${target.label}. جرّب المكان الآخر."
            record = if (firstTry) record.copy(firstChoiceWrong = record.firstChoiceWrong + 1)
            else record.copy(retries = record.retries + 1)
        }
    }

    TaskScaffold(
        title = "تجربة 2: أين يناسب؟",
        instruction = "ضع كل شيء مع الطقس المناسب له.",
        onBack = onBack,
        actions = {
            NuhaSecondaryButton("ابدأ من جديد", onClick = {
                placed.clear(); attempted.clear(); selectedId = null
                record = Record()
                feedback = "اختر شيئًا، ثم اختر مكانه."
            })
            Text(
                "صحيح من أول مرة: ${record.firstChoiceCorrect} · خطأ من أول مرة: ${record.firstChoiceWrong} · محاولات إضافية: ${record.retries}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                // Every item keeps its slot; a placed item leaves an empty space so nothing shifts under the child's finger.
                items.forEach { item ->
                    if (item.id in placed) {
                        Box(Modifier.weight(1f).fillMaxHeight())
                        return@forEach
                    }
                    ItemCard(
                        item = item,
                        selected = selectedId == item.id,
                        onSelect = {
                            selectedId = if (selectedId == item.id) null else item.id
                            feedback = if (selectedId != null) "اخترت ${item.name}. الآن اختر مكانها." else "اختر شيئًا، ثم اختر مكانه."
                        },
                        onPlace = { place(item, it) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }
            Text(
                if (placed.size == items.size) "انتهيت! 🎉" else feedback,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
            Row(Modifier.fillMaxWidth().weight(1.2f), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Place.entries.forEach { target ->
                    val inside = items.filter { placed[it.id] == target }
                    Destination(
                        place = target,
                        contents = inside,
                        enabled = selectedId != null,
                        onClick = { items.firstOrNull { it.id == selectedId }?.let { place(it, target) } },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemCard(item: Item, selected: Boolean, onSelect: () -> Unit, onPlace: (Place) -> Unit, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (selected) scheme.primaryContainer else scheme.surface,
        // Selection is shown by border thickness and a label, not by color alone.
        border = BorderStroke(if (selected) 6.dp else 2.dp, if (selected) scheme.primary else scheme.outline),
        modifier = modifier
            .sizeIn(minWidth = NuhaMinTarget, minHeight = NuhaMinTarget)
            // One accessible element per card: name, state, a tap action, and one-step "place with…" actions.
            .clearAndSetSemantics {
                role = Role.Button
                this.selected = selected
                contentDescription = item.name
                stateDescription = if (selected) "مختار" else "غير مختار"
                onClick(label = if (selected) "إلغاء الاختيار" else "اختيار") { onSelect(); true }
                customActions = Place.entries.map { p ->
                    CustomAccessibilityAction("ضع مع ${p.label}") { onPlace(p); true }
                }
            }
            .clickable(onClick = onSelect),
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(item.emoji, fontSize = 56.sp, modifier = Modifier.clearAndSetSemantics { })
            Text(if (selected) "✓ ${item.name}" else item.name, style = MaterialTheme.typography.titleLarge)
        }
    }
}

/** A destination drawn on a Canvas. A painted shape is invisible to TalkBack unless given semantics explicitly. */
@Composable
private fun Destination(place: Place, contents: List<Item>, enabled: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (place == Place.RAIN) scheme.tertiaryContainer else scheme.secondaryContainer,
        border = BorderStroke(if (enabled) 4.dp else 2.dp, if (enabled) scheme.primary else scheme.outline),
        modifier = modifier
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = "مكان ${place.label}"
                stateDescription = if (contents.isEmpty()) "فارغ" else "فيه: " + contents.joinToString("، ") { it.name }
                if (enabled) onClick(label = "ضع هنا") { onClick(); true } else disabled()
            }
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Canvas(Modifier.size(120.dp)) {
                if (place == Place.SUN) {
                    drawCircle(Color(0xFFE0A53A), radius = size.minDimension / 4)
                    repeat(8) { i ->
                        val a = Math.toRadians(i * 45.0)
                        val c = center
                        val r1 = size.minDimension / 3; val r2 = size.minDimension / 2.1f
                        drawLine(
                            Color(0xFFE0A53A),
                            Offset(c.x + (r1 * Math.cos(a)).toFloat(), c.y + (r1 * Math.sin(a)).toFloat()),
                            Offset(c.x + (r2 * Math.cos(a)).toFloat(), c.y + (r2 * Math.sin(a)).toFloat()),
                            strokeWidth = 8f, cap = StrokeCap.Round,
                        )
                    }
                } else {
                    val cloud = Color(0xFF7D8BB5)
                    drawCircle(cloud, radius = size.width / 5, center = Offset(size.width * 0.35f, size.height * 0.4f))
                    drawCircle(cloud, radius = size.width / 4, center = Offset(size.width * 0.6f, size.height * 0.35f))
                    repeat(3) { i ->
                        val x = size.width * (0.3f + i * 0.2f)
                        drawLine(Color(0xFF4A6FB5), Offset(x, size.height * 0.65f), Offset(x - 10f, size.height * 0.9f), strokeWidth = 8f, cap = StrokeCap.Round)
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(place.label, style = MaterialTheme.typography.headlineSmall)
                Text(contents.joinToString(" ") { it.emoji }.ifEmpty { "—" }, fontSize = 40.sp)
            }
        }
    }
}
