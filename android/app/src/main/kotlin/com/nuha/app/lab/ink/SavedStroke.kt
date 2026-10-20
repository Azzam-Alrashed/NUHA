package com.nuha.app.lab.ink

import androidx.compose.ui.unit.IntSize
import androidx.ink.brush.Brush
import androidx.ink.brush.InputToolType
import androidx.ink.strokes.MutableStrokeInputBatch
import androidx.ink.strokes.Stroke

/** One input sample, stored relative to the canvas (0..1) so a saved stroke can be replayed at any size. */
data class SavedPoint(val x: Float, val y: Float, val elapsedMs: Long)

data class SavedStroke(val toolType: InputToolType, val points: List<SavedPoint>)

internal fun Stroke.toSaved(canvas: IntSize): SavedStroke {
    val w = canvas.width.coerceAtLeast(1).toFloat()
    val h = canvas.height.coerceAtLeast(1).toFloat()
    val points = (0 until inputs.size).map { i ->
        val p = inputs.get(i)
        SavedPoint(p.x / w, p.y / h, p.elapsedTimeMillis)
    }
    return SavedStroke(inputs.getToolType(), points)
}

internal fun SavedStroke.toStroke(brush: Brush, canvas: IntSize): Stroke {
    val batch = MutableStrokeInputBatch()
    points.forEach { batch.add(toolType, it.x * canvas.width, it.y * canvas.height, it.elapsedMs) }
    return Stroke(brush, batch)
}

internal fun InputToolType.label() = when (this) {
    InputToolType.STYLUS -> "قلم"
    InputToolType.TOUCH -> "إصبع"
    InputToolType.MOUSE -> "فأرة"
    else -> "غير معروف"
}
