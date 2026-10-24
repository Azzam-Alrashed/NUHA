package com.nuha.app.activities

import androidx.compose.ui.geometry.Offset
import com.nuha.app.Pending
import com.nuha.app.child.Activity
import com.nuha.app.task.StrokePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The nine activity plans (FR-ACT-012 and the FR-EMO … FR-BAL items) and the route geometry, without a device. */
class ActivitiesTest {
    private fun settings(difficulty: String) = difficulty.split(';').associate { it.substringBefore('=') to it.substringAfter('=') }

    @Test fun everyActivityHasAPlanThatFillsTheBoard() {
        for (activity in Activity.entries) for (completed in 0..5) {
            val plan = planFor(activity, completed, Pending.DefaultNumerals)
            assertEquals(activity, plan.activity)
            assertEquals("$activity", plan.slots, plan.trials(7).size)
        }
    }

    @Test fun theSameSeedGivesTheSameTrials() {
        for (activity in Activity.entries) {
            val plan = planFor(activity, 1, Pending.DefaultNumerals)
            assertEquals("$activity", plan.trials(99).map { it.key }, plan.trials(99).map { it.key })
            assertEquals("$activity", plan.trials(99).map { it.instruction }, plan.trials(99).map { it.instruction })
        }
    }

    /** Each step up changes one setting (Patterns alternates modes, so its levels move every second session). */
    @Test fun levelsChangeOneFactorAtATime() {
        for (activity in Activity.entries) {
            val step = if (activity == Activity.Patterns) 2 else 1
            for (completed in 0..6) {
                val before = settings(planFor(activity, completed, Pending.DefaultNumerals).difficulty) - "level"
                val after = settings(planFor(activity, completed + step, Pending.DefaultNumerals).difficulty) - "level"
                val changed = (before.keys + after.keys).filter { before[it] != after[it] }
                assertTrue("$activity $completed: $changed", changed.size <= 1)
            }
        }
    }

    @Test fun levelsMoveUpAndStopAtTheTop() {
        for (activity in Activity.entries) {
            val levels = (0..8).map { settings(planFor(activity, it, Pending.DefaultNumerals).difficulty).getValue("level").toInt() }
            assertEquals("$activity", levels, levels.sorted())
            assertTrue("$activity", levels.last() > levels.first())
        }
    }

    @Test fun choiceItemsAlwaysOfferTheAnswer() {
        for (completed in 0..3) for (seed in 1L..20L) {
            planFor(Activity.Emotions, completed, Pending.DefaultNumerals).trials(seed).forEach { val i = it.item as EmotionItem; assertTrue(i.feeling in i.options); assertEquals(i.options.size, i.options.toSet().size) }
            planFor(Activity.Patterns, completed, Pending.DefaultNumerals).trials(seed).forEach { val i = it.item as PatternItem; assertTrue(i.answer in i.options) }
            planFor(Activity.Letters, completed, Pending.DefaultNumerals).trials(seed).forEach { val i = it.item as LetterItem; assertTrue(i.target in i.options) }
            planFor(Activity.Environment, completed, Pending.DefaultNumerals).trials(seed).forEach { val i = it.item as PlaceItem; assertTrue(i.places.any { p -> p.id in i.thing.accepted }) }
        }
    }

    @Test fun balanceSumsStayInRangeWithDistinctChoices() {
        for (completed in 0..3) {
            val plan = BalancePlan(completed)
            val s = settings(plan.difficulty)
            for (seed in 1L..30L) plan.trials(seed).forEach {
                val i = it.item as BalanceItem
                assertTrue(i.a >= 1 && i.b >= 1 && i.sum <= s.getValue("maxSum").toInt())
                assertTrue(i.sum in i.options)
                assertEquals(s.getValue("choices").toInt(), i.options.toSet().size)
            }
        }
    }

    @Test fun patternsContinueTheRule() {
        for (completed in 0..5) for (seed in 1L..10L) PatternsPlan(completed).trials(seed).forEach {
            val i = it.item as PatternItem
            val period = mapOf("AB" to 2, "AAB" to 3, "ABC" to 3).getValue(settings(PatternsPlan(completed).difficulty).getValue("rule"))
            val full = i.sequence + i.answer
            full.indices.drop(period).forEach { k -> assertEquals(full[k - period], full[k]) }
        }
        assertEquals("continue", settings(PatternsPlan(0).difficulty)["mode"])
        assertEquals("draw", settings(PatternsPlan(1).difficulty)["mode"])
    }

    @Test fun gardenSeedsHaveDistinctPlaces() {
        for (completed in 0..3) GardenPlan(completed).trials(3).forEach {
            val i = it.item as GardenItem
            assertEquals(i.count, i.spots.size)
            assertEquals(i.count, i.spots.toSet().size)
        }
    }

    private fun line(vararg xy: Pair<Float, Float>, n: Int = 40) = xy.toList().zipWithNext { (ax, ay), (bx, by) ->
        List(n) { k -> StrokePoint(ax + (bx - ax) * k / n, ay + (by - ay) * k / n, 0) }
    }.flatten() + xy.last().let { StrokePoint(it.first, it.second, 0) }

    @Test fun routeMustJoinBothEndsAndAvoidObstacles() {
        val rock = listOf(Circle(.5f, .5f, .16f))
        val straight = line(.88f to .5f, .12f to .5f)
        val around = line(.88f to .5f, .5f to .12f, .12f to .5f)
        assertEquals(setOf(0), checkRoute(straight, .88f, .5f, .12f, .5f, rock, aspect = 1.6f).touched)
        assertTrue(checkRoute(around, .88f, .5f, .12f, .5f, rock, aspect = 1.6f).clear)
        assertFalse(checkRoute(line(.88f to .5f, .5f to .12f), .88f, .5f, .12f, .5f, rock, aspect = 1.6f).connects)
        assertFalse(checkRoute(emptyList(), .88f, .5f, .12f, .5f, rock, aspect = 1.6f).connects)
    }

    @Test fun ballFallsIntoTheFirstHoleOnItsRouteEveryTime() {
        val holes = listOf(Circle(.6f, .5f, .13f), Circle(.3f, .5f, .13f))
        val straight = line(.88f to .5f, .12f to .5f)
        val fall = rollBall(straight, holes, aspect = 1.6f)
        assertNotNull(fall)
        assertEquals(0, fall!!.first)
        assertEquals(fall, rollBall(straight, holes, aspect = 1.6f))
        assertTrue(straight[fall.second].x > .6f)
        assertNull(rollBall(line(.88f to .5f, .45f to .1f, .12f to .5f), holes, aspect = 1.6f))
    }

    @Test fun starMovesAlongThePathByLength() {
        val path = listOf(Offset(0f, 0f), Offset(1f, 0f), Offset(1f, 1f))
        assertEquals(Offset(0f, 0f), along(path, 0f))
        assertEquals(Offset(1f, 0f), along(path, .5f))
        assertEquals(Offset(1f, .5f), along(path, .75f))
        assertEquals(Offset(1f, 1f), along(path, 1f))
        assertEquals(Offset(1f, 1f), along(path, 2f))
    }

    @Test fun placeholdersKeepTheirDocumentedDefaults() {
        assertEquals("route", Pending.MazeMechanic)
        assertFalse(Pending.GardenTimed)
        assertEquals(listOf("continue", "draw", "continue"), (0..2).map(Pending::patternMode))
    }

    /** Balance and garden write their numbers in the child's numerals (FR-SET-002). */
    @Test fun plansUseTheChildsNumerals() {
        val balance = BalancePlan(0, com.nuha.app.Numerals.ArabicIndic).trials(5)
        assertTrue(balance.all { t -> t.praise.none { it in '0'..'9' } && t.praise.any { it in '٠'..'٩' } })
        assertTrue(GardenPlan(0, com.nuha.app.Numerals.ArabicIndic).trials(5).all { "١" in it.instruction })
        assertTrue(GardenPlan(0, com.nuha.app.Numerals.Western).trials(5).all { "1" in it.instruction })
    }
}
