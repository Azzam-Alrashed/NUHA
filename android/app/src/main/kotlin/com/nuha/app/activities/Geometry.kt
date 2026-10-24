package com.nuha.app.activities

import com.nuha.app.task.StrokePoint
import kotlin.math.hypot

// Plain geometry for the route activities, in 0..1 card coordinates, measured in units of the card's shorter side
// ([aspect] = width ÷ height) so circles stay round. Kept free of UI so it can be unit-tested.

/** A round obstacle or hole. [r] is in units of the card's shorter side. */
data class Circle(val x: Float, val y: Float, val r: Float)

private fun dist(ax: Float, ay: Float, bx: Float, by: Float, aspect: Float): Float {
    val sx = if (aspect >= 1f) aspect else 1f
    val sy = if (aspect >= 1f) 1f else 1f / aspect
    return hypot((ax - bx) * sx, (ay - by) * sy)
}

/** [connects]: starts within [reach] of [startX],[startY] and ends within [reach] of the end. [touched]: obstacles crossed. */
data class RouteCheck(val connects: Boolean, val touched: Set<Int>) {
    val clear: Boolean get() = connects && touched.isEmpty()
}

fun checkRoute(route: List<StrokePoint>, startX: Float, startY: Float, endX: Float, endY: Float, obstacles: List<Circle>, aspect: Float, reach: Float = .16f): RouteCheck {
    if (route.isEmpty()) return RouteCheck(false, emptySet())
    val first = route.first(); val last = route.last()
    val connects = dist(first.x, first.y, startX, startY, aspect) <= reach && dist(last.x, last.y, endX, endY, aspect) <= reach
    val touched = obstacles.indices.filter { i -> val o = obstacles[i]; route.any { dist(it.x, it.y, o.x, o.y, aspect) < o.r } }.toSet()
    return RouteCheck(connects, touched)
}

/**
 * The maze ball rolls along [route] in order. Returns the index of the hole it falls into and the route point where
 * that happens, or null when it rolls the whole way. Deterministic: the same drawing always gives the same result.
 */
fun rollBall(route: List<StrokePoint>, holes: List<Circle>, aspect: Float): Pair<Int, Int>? {
    for ((pi, p) in route.withIndex()) for ((hi, h) in holes.withIndex()) if (dist(p.x, p.y, h.x, h.y, aspect) < h.r) return hi to pi
    return null
}
