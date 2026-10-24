package com.nuha.app.task

import com.nuha.app.Pending
import kotlin.math.hypot

/** One input sample, relative to the drawing area (0..1 on each axis), so a stroke replays at any size (FR-ACT-017). */
data class StrokePoint(val x: Float, val y: Float, val elapsedMs: Long)

data class SavedStroke(val tool: InputType, val points: List<StrokePoint>)

/**
 * Text form of strokes for the trial record: one stroke per line, "tool|x,y,ms;x,y,ms;…", coordinates to 4 decimals.
 * Plain text keeps records readable and needs no extra library.
 */
object StrokeCodec {
    fun encode(strokes: List<SavedStroke>): String = strokes.joinToString("\n") { s ->
        s.tool.id + "|" + s.points.joinToString(";") { "%.4f,%.4f,%d".format(java.util.Locale.ROOT, it.x, it.y, it.elapsedMs) }
    }

    fun decode(text: String): List<SavedStroke> = text.lineSequence().filter { it.isNotBlank() }.map { line ->
        val (tool, pts) = line.split("|", limit = 2)
        SavedStroke(
            InputType.entries.firstOrNull { it.id == tool } ?: InputType.Other,
            pts.split(";").filter { it.isNotBlank() }.map { p -> p.split(",").let { StrokePoint(it[0].toFloat(), it[1].toFloat(), it[2].toLong()) } },
        )
    }.toList()
}

/**
 * How well a drawing follows a guide line, in normalised coordinates (FR-ACT-018). [coverage]: share of guide
 * samples with a drawn point within the tolerance. [stray]: share of drawn points farther than the tolerance from
 * the guide. Reasonable variation passes; exact pixel overlap is never required.
 */
data class TraceScore(val coverage: Float, val stray: Float) {
    val accepted: Boolean get() = coverage >= Pending.TraceCoverage && stray <= Pending.TraceStray
}

/**
 * Scores [strokes] against [guide], both 0..1. [aspect] is width ÷ height of the drawing area, so distance is measured
 * in true proportions; [tolerance] is a fraction of the shorter side.
 */
fun traceScore(guide: List<StrokePoint>, strokes: List<SavedStroke>, aspect: Float, tolerance: Float = Pending.TraceTolerance): TraceScore {
    val drawn = strokes.flatMap { it.points }
    if (guide.isEmpty() || drawn.isEmpty()) return TraceScore(0f, 1f)
    // Measure in units of the shorter side.
    val sx = if (aspect >= 1f) aspect else 1f
    val sy = if (aspect >= 1f) 1f else 1f / aspect
    fun dist(a: StrokePoint, b: StrokePoint) = hypot((a.x - b.x) * sx, (a.y - b.y) * sy)
    val dense = densify(guide, step = tolerance / 2f, sx, sy)
    val covered = dense.count { g -> drawn.any { dist(it, g) <= tolerance } }
    val stray = drawn.count { d -> dense.none { dist(it, d) <= tolerance } }
    return TraceScore(covered.toFloat() / dense.size, stray.toFloat() / drawn.size)
}

/** Adds samples along the guide so no gap between points is wider than [step]. */
private fun densify(guide: List<StrokePoint>, step: Float, sx: Float, sy: Float): List<StrokePoint> {
    val out = mutableListOf(guide.first())
    for (i in 1 until guide.size) {
        val a = guide[i - 1]; val b = guide[i]
        val n = (hypot((b.x - a.x) * sx, (b.y - a.y) * sy) / step).toInt().coerceAtLeast(1)
        for (k in 1..n) { val t = k / n.toFloat(); out += StrokePoint(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, 0) }
    }
    return out
}
