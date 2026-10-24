package com.nuha.app.task

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.ink.authoring.compose.InProgressStrokes
import androidx.ink.brush.Brush
import androidx.ink.brush.InputToolType
import androidx.ink.brush.StockBrushes
import androidx.ink.geometry.AffineTransform
import androidx.ink.rendering.android.canvas.CanvasStrokeRenderer
import androidx.ink.strokes.MutableStrokeInputBatch
import androidx.ink.strokes.Stroke

/**
 * A drawing surface on the Ink API (FR-ACT-017, RB §11). Finished strokes are handed to [onStroke] in saved form
 * (0..1 points with time and tool) and drawn back from [strokes], so what the child sees is always the replay of the
 * saved data, at whatever size the surface has. Undo and clear are the caller removing items from [strokes].
 * Ink 1.0 accepts any pointer; stylus-only and palm rejection need the real table (IR-HW-002).
 */
@Composable
fun InkSurface(strokes: List<SavedStroke>, color: Color, widthDp: Float, enabled: Boolean, onStroke: (SavedStroke) -> Unit, modifier: Modifier = Modifier) {
    val brush = remember(color, widthDp) { Brush.createWithColorIntArgb(StockBrushes.pressurePen(), color.toArgb(), widthDp * 2.6f, .1f) }
    val renderer = remember { CanvasStrokeRenderer.create() }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val finished by rememberUpdatedState(onStroke)
    // Keyed on a copy, so adding to or removing from a state list rebuilds the drawing.
    val saved = strokes.toList()
    val rendered = remember(saved, size, brush) { if (size == IntSize.Zero) emptyList() else saved.map { it.toInk(brush, size) } }
    Box(modifier.onSizeChanged { size = it }) {
        Canvas(Modifier.fillMaxSize()) {
            drawIntoCanvas { canvas -> rendered.forEach { renderer.draw(canvas.nativeCanvas, it, AffineTransform.IDENTITY) } }
        }
        if (enabled) InProgressStrokes(defaultBrush = brush, onStrokesFinished = { done -> done.forEach { finished(it.toSaved(size)) } })
    }
}

private fun Stroke.toSaved(canvas: IntSize): SavedStroke {
    val w = canvas.width.coerceAtLeast(1).toFloat()
    val h = canvas.height.coerceAtLeast(1).toFloat()
    val points = (0 until inputs.size).map { i -> inputs.get(i).let { StrokePoint(it.x / w, it.y / h, it.elapsedTimeMillis) } }
    return SavedStroke(inputs.getToolType().toInput(), points)
}

private fun SavedStroke.toInk(brush: Brush, canvas: IntSize): Stroke {
    val batch = MutableStrokeInputBatch()
    val tool = when (tool) { InputType.Stylus -> InputToolType.STYLUS; InputType.Finger -> InputToolType.TOUCH; InputType.Other -> InputToolType.UNKNOWN }
    points.forEach { batch.add(tool, it.x * canvas.width, it.y * canvas.height, it.elapsedMs) }
    return Stroke(brush, batch)
}

private fun InputToolType.toInput(): InputType = when (this) {
    InputToolType.STYLUS -> InputType.Stylus
    InputToolType.TOUCH -> InputType.Finger
    else -> InputType.Other
}
