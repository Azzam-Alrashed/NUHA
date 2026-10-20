package com.nuha.app.lab.ink

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.ink.authoring.compose.InProgressStrokes
import androidx.ink.brush.Brush
import androidx.ink.brush.InputToolType
import androidx.ink.brush.StockBrushes
import androidx.ink.geometry.AffineTransform
import androidx.ink.rendering.android.canvas.CanvasStrokeRenderer
import androidx.ink.strokes.MutableStrokeInputBatch
import androidx.ink.strokes.Stroke
import com.nuha.app.ui.components.NuhaPrimaryButton
import com.nuha.app.ui.components.NuhaSecondaryButton
import com.nuha.app.ui.components.TaskScaffold

/** One input sample, stored relative to the canvas (0..1) so a saved stroke can be replayed at any size. */
data class SavedPoint(val x: Float, val y: Float, val elapsedMs: Long)

data class SavedStroke(val toolType: InputToolType, val points: List<SavedPoint>)

private fun Stroke.toSaved(canvas: IntSize): SavedStroke {
    val w = canvas.width.coerceAtLeast(1).toFloat()
    val h = canvas.height.coerceAtLeast(1).toFloat()
    val points = (0 until inputs.size).map { i ->
        val p = inputs.get(i)
        SavedPoint(p.x / w, p.y / h, p.elapsedTimeMillis)
    }
    return SavedStroke(inputs.getToolType(), points)
}

private fun SavedStroke.toStroke(brush: Brush, canvas: IntSize): Stroke {
    val batch = MutableStrokeInputBatch()
    points.forEach { batch.add(toolType, it.x * canvas.width, it.y * canvas.height, it.elapsedMs) }
    return Stroke(brush, batch)
}

private fun InputToolType.label() = when (this) {
    InputToolType.STYLUS -> "قلم"
    InputToolType.TOUCH -> "إصبع"
    InputToolType.MOUSE -> "فأرة"
    else -> "غير معروف"
}

/**
 * Experiment 1: a short letter-tracing task on the Ink API.
 * Checks: ink latency, undo/clear, stylus vs finger detection, and replaying saved strokes
 * after the canvas changes size (rotation, window resize).
 */
@Composable
fun TraceExperimentScreen(onBack: () -> Unit) {
    val color = MaterialTheme.colorScheme.primary
    val brush = remember(color) {
        Brush.createWithColorIntArgb(StockBrushes.pressurePen(), color.toArgb(), 18f, 0.1f)
    }
    val renderer = remember { CanvasStrokeRenderer.create() }

    // The saved form is the source of truth; rendered strokes are rebuilt from it when the size changes.
    val saved = remember { mutableStateListOf<SavedStroke>() }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var rendered by remember { mutableStateOf<List<Stroke>>(emptyList()) }
    var lastReplayNote by remember { mutableStateOf("") }

    fun rebuild() {
        rendered = saved.map { it.toStroke(brush, canvasSize) }
    }

    TaskScaffold(
        title = "تجربة 1: تتبّع حرف بالقلم",
        instruction = "تتبّع حرف الباء بالقلم.",
        onBack = onBack,
        actions = {
            NuhaSecondaryButton("تراجع", enabled = saved.isNotEmpty(), onClick = {
                saved.removeAt(saved.lastIndex); rebuild()
            })
            NuhaSecondaryButton("مسح", enabled = saved.isNotEmpty(), onClick = {
                saved.clear(); rebuild()
            })
            NuhaPrimaryButton("إعادة العرض من المحفوظ", enabled = saved.isNotEmpty(), onClick = {
                rendered = emptyList()
                rebuild()
                lastReplayNote = "أُعيد بناء ${saved.size} خط من البيانات المحفوظة بحجم \u2066${canvasSize.width}×${canvasSize.height}\u2069 بكسل"
            })
        },
    ) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large,
                shadowElevation = 2.dp,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .onSizeChanged {
                            if (it != canvasSize) {
                                canvasSize = it
                                rebuild()
                            }
                        }
                        .semantics { contentDescription = "مساحة الرسم. ${saved.size} خطوط مرسومة." },
                ) {
                    // Guide letter. A real task would use an educator-approved stroke-order guide.
                    val guideSize = with(LocalDensity.current) { (canvasSize.height * 0.6f).toSp() }
                    Text(
                        "ب",
                        fontSize = guideSize,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    Canvas(Modifier.fillMaxSize()) {
                        drawIntoCanvas { canvas ->
                            rendered.forEach { renderer.draw(canvas.nativeCanvas, it, AffineTransform.IDENTITY) }
                        }
                    }
                    InProgressStrokes(
                        defaultBrush = brush,
                        onStrokesFinished = { finished ->
                            saved.addAll(finished.map { it.toSaved(canvasSize) })
                            rendered = rendered + finished
                        },
                    )
                }
            }
            StrokeStats(saved, lastReplayNote, Modifier.width(300.dp).fillMaxHeight())
        }
    }
}

@Composable
private fun StrokeStats(saved: List<SavedStroke>, replayNote: String, modifier: Modifier) {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium, modifier = modifier) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("قياسات التجربة", style = MaterialTheme.typography.titleMedium)
            Text("عدد الخطوط: ${saved.size}", style = MaterialTheme.typography.bodyMedium)
            val last = saved.lastOrNull()
            if (last != null) {
                val duration = last.points.lastOrNull()?.elapsedMs ?: 0
                Text("آخر خط: ${last.points.size} نقطة", style = MaterialTheme.typography.bodyMedium)
                Text("المدة: $duration ملّي ثانية", style = MaterialTheme.typography.bodyMedium)
                Text("أداة الإدخال: ${last.toolType.label()}", style = MaterialTheme.typography.bodyMedium)
            }
            val tools = saved.groupingBy { it.toolType.label() }.eachCount()
            if (tools.isNotEmpty()) {
                Text("حسب الأداة: " + tools.entries.joinToString("، ") { "${it.key} ${it.value}" }, style = MaterialTheme.typography.bodySmall)
            }
            if (replayNote.isNotEmpty()) {
                Text(replayNote, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    }
}
