package com.nuha.app

/**
 * Marks Arabic content (letters, words, questions, voice lines, child-facing labels) that is a draft until an
 * Arabic early-years educator reviews it (SRS NFR-LOC-003, FR-AUD-003). Find all of it with `grep -rn @DraftArabic`.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class DraftArabic

/**
 * Stand-ins for decisions the SRS leaves open (Appendix B). Each value is a placeholder chosen only so the app runs;
 * changing a decision should mean changing it here. Every entry names its open issue; grep "PLACEHOLDER OI-".
 */
object Pending {
    /** PLACEHOLDER OI-01: age range. The legacy form allows 2–6 with 4 preselected; the plan targets 4–10. */
    val AgeRange = 2..6
    const val DefaultAge = 4

    /**
     * PLACEHOLDER OI-02: numeral system. FR-SET-002 makes numerals a per-child setting; this is the default for new
     * children and for screens with no child (onboarding, the adult area). Show numbers with [LocalNumerals].
     */
    val DefaultNumerals = Numerals.Western

    /** PLACEHOLDER OI-03: the guide's name. The product name stands in. */
    @DraftArabic const val GuideName = "نُهى"

    /** PLACEHOLDER OI-05: an area leads the starting path when its score reaches this share of its maximum... */
    const val PathLeadMin = .5f
    /** ...and is at least this far ahead of the next area; otherwise the path is mixed. */
    const val PathLeadMargin = .15f

    /** PLACEHOLDER OI-19 (session length): star slots on the token board, within the 3–5 of FR-RWD-001. */
    const val TokenSlots = 5

    /**
     * PLACEHOLDER (FR-SET-002 lists prompt delay as a per-child setting but gives no values): the choices, in seconds of
     * quiet active time before the prompt ladder steps up by itself; 0 means never. The default is the middle one.
     */
    val PromptDelayChoices = listOf(5, 10, 20, 0)
    const val DefaultPromptDelaySeconds = 10

    /**
     * PLACEHOLDER (SRS silent on how prompts fade): a trial starts one level below the highest level the previous
     * trial needed, so help fades as the child succeeds; a repeat-only level fades straight to none.
     */
    fun fadedStart(previousLevel: Int): Int = if (previousLevel >= 2) previousLevel - 1 else 0

    /**
     * PLACEHOLDER (FR-SET-002 lists target size but gives no values; NFR-ACC-003 sets 96–120 dp for primary targets):
     * a scale on everything inside a task's play area.
     */
    val TargetScales = linkedMapOf("standard" to 1f, "large" to 1.15f)

    /**
     * PLACEHOLDER OI-14: motion for a new child while the system's animations are on. FR-SET-004 says follow the
     * system setting; ui-style.md's calm default is reduced. With system animations off a new child starts at none.
     */
    const val DefaultMotion = "reduced"

    /**
     * PLACEHOLDER OI-14 / FR-SET-005: what each sensory starting point sets. ui-style.md names lively, gentle and muted
     * but not their values. Voice stays on and music off in all three (FR-SET-006).
     */
    fun sensoryPreset(point: String, childId: Long, systemMotionOff: Boolean): com.nuha.app.data.ChildSettings {
        val base = com.nuha.app.data.ChildSettings(childId)
        val preset = when (point) {
            "lively" -> base.copy(appearance = "colourful", motion = "full", effects = true, celebration = "lively")
            "muted" -> base.copy(appearance = "muted", motion = "none", effects = false, celebration = "off")
            else -> base.copy(appearance = "colourful", motion = DefaultMotion, effects = true, celebration = "gentle")
        }
        return if (systemMotionOff) preset.copy(motion = "none") else preset
    }

    /** PLACEHOLDER OI-14: the child-first onboarding has no adult step yet, so its child starts at this sensory point. */
    const val OnboardingSensory = "gentle"

    /**
     * PLACEHOLDER OI-15: the gate. The adult holds the «للكبار» button this long, then types a number shown in words.
     * A wrong number closes the gate, so random input has to hold again before it can try again.
     */
    const val GateHoldMillis = 2_000L
    val GateNumbers = 100..999

    /** PLACEHOLDER (SRS silent): map stars per activity, one per completed session, up to three (FR-RWD-008). */
    fun starsFor(completedSessions: Int): Int = completedSessions.coerceIn(0, 3)

    /**
     * PLACEHOLDER (FR-ACT-018; thresholds need educator-drawn samples): a traced line is accepted when this share of
     * the guide is covered within the tolerance...
     */
    const val TraceCoverage = .8f
    /** ...and no more than this share of the drawing strays outside it. */
    const val TraceStray = .3f
    /** Tolerance as a fraction of the drawing area's shorter side. */
    const val TraceTolerance = .06f

    /**
     * PLACEHOLDER (SRS silent on progression; FR-ACT-012 asks only for one factor at a time): an activity's level is the
     * number of sessions the child has completed on it, capped at the activity's top level.
     */
    fun levelFor(completedSessions: Int, top: Int): Int = completedSessions.coerceIn(0, top)

    /** PLACEHOLDER (FR-STR-001; needs observation on the table): share of contact time inside the ring that solves a star run. */
    const val StarInTargetMin = .6f

    /** PLACEHOLDER OI-10 (maze mechanic): "route", the child draws the ball's path (legacy); the plan describes "walls". */
    const val MazeMechanic = "route"

    /** PLACEHOLDER (FR-GDN-001 allows timing only as a separately chosen mode; the SRS lists no setting for it): the garden runs untimed. */
    const val GardenTimed = false

    /** PLACEHOLDER (FR-PAT-001 modes are separate; who picks is open): sessions alternate continuation and drawing. */
    fun patternMode(completedSessions: Int) = if (completedSessions % 2 == 0) "continue" else "draw"

    /**
     * PLACEHOLDER OI-20: the road stop each starting path begins at (FR-HOM-005): the first activity of the matching
     * area in the plan's order. Mixed begins at the first stop on the road.
     */
    fun startStop(path: String): com.nuha.app.child.Activity = when (path) {
        "attention" -> com.nuha.app.child.Activity.Star
        "learning" -> com.nuha.app.child.Activity.Letters
        else -> com.nuha.app.child.Activity.Emotions
    }
}
