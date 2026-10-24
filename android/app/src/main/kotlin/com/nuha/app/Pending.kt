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

    /** PLACEHOLDER OI-02: numeral system. Western digits until Western vs Arabic-Indic is decided; use this for every number shown. */
    fun numeral(n: Int): String = n.toString()

    /** PLACEHOLDER OI-03: the guide's name. The product name stands in. */
    @DraftArabic const val GuideName = "نُهى"

    /** PLACEHOLDER OI-05: an area leads the starting path when its score reaches this share of its maximum... */
    const val PathLeadMin = .5f
    /** ...and is at least this far ahead of the next area; otherwise the path is mixed. */
    const val PathLeadMargin = .15f

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
