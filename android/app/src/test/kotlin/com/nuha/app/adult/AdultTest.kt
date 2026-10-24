package com.nuha.app.adult

import com.nuha.app.Numerals
import com.nuha.app.Pending
import com.nuha.app.data.ChildSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** The gate (FR-ADM-002, placeholder OI-15), sensory starting points (FR-SET-005) and numerals (FR-SET-002). */
class AdultTest {
    @Test fun numberWordsReadAsArabic() {
        assertEquals("مئة", NumberWords.of(100))
        assertEquals("مئتان", NumberWords.of(200))
        assertEquals("ثلاثمئة وخمسة وأربعون", NumberWords.of(345))
        assertEquals("سبعمئة واثنان", NumberWords.of(702))
        assertEquals("خمسمئة وعشرة", NumberWords.of(510))
        assertEquals("تسعمئة وأحد عشر", NumberWords.of(911))
        assertEquals("ستمئة وستون", NumberWords.of(660))
    }

    /** Every gate number has its own words, so reading them gives exactly one answer. */
    @Test fun everyGateNumberHasDistinctWordsWithNoDigits() {
        val all = Pending.GateNumbers.map(NumberWords::of)
        assertEquals(all.size, all.toSet().size)
        assertTrue(all.none { w -> w.any(Char::isDigit) })
    }

    @Test fun gateWaitsForAllDigitsThenPassesOrFails() {
        assertEquals(GateResult.Typing, gateResult(345, "34"))
        assertEquals(GateResult.Pass, gateResult(345, "345"))
        assertEquals(GateResult.Fail, gateResult(345, "354"))
    }

    /**
     * Random keypad input against the gate's rule that one wrong number closes it: in 10,000 random key presses a
     * guess passes about once in 900 tries, which is why the gate also needs a two-second hold before each try.
     * This checks the keypad rule alone never passes more often than chance allows.
     */
    @Test fun randomTypingRarelyGuessesTheNumber() {
        val r = Random(7)
        var passes = 0; var tries = 0
        var number = Pending.GateNumbers.random(r); var typed = ""
        repeat(10_000) {
            typed += r.nextInt(10)
            when (gateResult(number, typed)) {
                GateResult.Typing -> Unit
                GateResult.Pass -> { passes++; tries++; typed = ""; number = Pending.GateNumbers.random(r) }
                GateResult.Fail -> { tries++; typed = ""; number = Pending.GateNumbers.random(r) }
            }
        }
        assertTrue("$passes passes in $tries tries", passes <= tries / 300 + 1)
    }

    @Test fun sensoryPresetsKeepVoiceOnAndMusicOff() {
        for (point in listOf("lively", "gentle", "muted")) {
            val s = Pending.sensoryPreset(point, 3, systemMotionOff = false)
            assertEquals(3, s.childId)
            assertTrue(s.voice)
            assertFalse(s.music)
        }
        assertEquals("full", Pending.sensoryPreset("lively", 1, false).motion)
        assertEquals("muted", Pending.sensoryPreset("muted", 1, false).appearance)
        assertEquals("off", Pending.sensoryPreset("muted", 1, false).celebration)
        assertEquals("gentle", Pending.sensoryPreset("gentle", 1, false).celebration)
    }

    /** FR-SET-004: with the system's animations off a new child starts with no motion, whatever the starting point. */
    @Test fun systemAnimationsOffMeansNoMotion() {
        for (point in listOf("lively", "gentle", "muted")) assertEquals("none", Pending.sensoryPreset(point, 1, systemMotionOff = true).motion)
    }

    /** FR-SET-006 and FR-RWD-005 defaults for a child with no saved choices. */
    @Test fun settingsDefaults() {
        val s = ChildSettings(childId = 1)
        assertTrue(s.voice)
        assertFalse(s.music)
        assertEquals("gentle", s.celebration)
        assertEquals(null, s.mapLock)
    }

    @Test fun numeralsFormat() {
        assertEquals("120", Numerals.Western.format(120))
        assertEquals("١٢٠", Numerals.ArabicIndic.format(120))
        assertEquals(Numerals.ArabicIndic, Numerals.of("arabic-indic"))
        assertEquals(Pending.DefaultNumerals, Numerals.of("unknown"))
    }
}
