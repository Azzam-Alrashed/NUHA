package com.nuha.app.task

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Saved strokes round-trip (FR-ACT-017) and the tracing tolerance accepts variation but not stray drawing (FR-ACT-018). */
class StrokesTest {
    private val guide = List(21) { i -> StrokePoint(.9f - .8f * i / 20f, .5f, 0) }

    @Test fun codecRoundTrip() {
        val strokes = listOf(
            SavedStroke(InputType.Stylus, listOf(StrokePoint(.1234f, .5f, 0), StrokePoint(.2f, .25f, 16))),
            SavedStroke(InputType.Finger, listOf(StrokePoint(1f, 0f, 3))),
        )
        assertEquals(strokes, StrokeCodec.decode(StrokeCodec.encode(strokes)))
    }

    @Test fun closeTracingWithAWobbleIsAccepted() {
        val wobbly = SavedStroke(InputType.Finger, List(40) { i -> StrokePoint(.9f - .8f * i / 39f, .5f + if (i % 2 == 0) .02f else -.02f, i * 16L) })
        assertTrue(traceScore(guide, listOf(wobbly), aspect = 1.6f).accepted)
    }

    @Test fun halfALineIsNotAccepted() {
        val half = SavedStroke(InputType.Finger, List(20) { i -> StrokePoint(.9f - .4f * i / 19f, .5f, 0) })
        val score = traceScore(guide, listOf(half), aspect = 1.6f)
        assertFalse(score.accepted)
        assertTrue(score.coverage < .7f)
    }

    @Test fun scribblingEverywhereIsNotAccepted() {
        val scribble = SavedStroke(InputType.Finger, List(200) { i -> StrokePoint((i % 20) / 20f, (i / 20) / 10f, 0) })
        assertFalse(traceScore(guide, listOf(scribble), aspect = 1.6f).accepted)
    }

    @Test fun nothingDrawnScoresZero() {
        val score = traceScore(guide, emptyList(), aspect = 1f)
        assertEquals(0f, score.coverage)
        assertFalse(score.accepted)
    }
}
