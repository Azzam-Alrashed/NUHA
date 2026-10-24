package com.nuha.app.child

import com.nuha.app.DraftArabic
import com.nuha.app.onboarding.Area
import com.nuha.app.ui.identity.Hue
import com.nuha.app.ui.identity.IdentityPalette

/**
 * The nine activities of the original plan (SRS §3.1.5), in their fixed order along the home road (FR-HOM-002).
 * [label] is the short name a child sees under a stop; [fullName] is the plan's name, for adults and reports.
 * [area] groups activities by starting path; the grouping follows the plan's three sets and is PLACEHOLDER OI-20.
 * [mascot] is the shape buddy that represents the activity; PLACEHOLDER OI-12 (mascot cast) until the cast is decided.
 */
enum class Activity(
    val id: String,
    @DraftArabic val label: String,
    @DraftArabic val fullName: String,
    val pictogram: String,
    val area: Area,
    val mascot: String,
) {
    Emotions("emotions", "المشاعر", "تلوين المشاعر", "emotions", Area.Communication, "clover"),
    Environment("environment", "البيئة", "رحلة مطابقة البيئة", "environment", Area.Communication, "squircle"),
    SocialPath("path", "المسار", "تتبع مسار الأشكال الاجتماعية", "path", Area.Communication, "dome"),
    Star("star", "النجمة", "تتبع النجوم الهادئة", "star", Area.Attention, "sun"),
    Maze("maze", "المتاهة", "متاهة توازن الكرة", "maze", Area.Attention, "bean"),
    Garden("garden", "البستان", "زرع بستان الأزهار", "garden", Area.Attention, "clover"),
    Letters("letters", "الحروف", "كتابة الحروف وتوصيل الكلمات", "letters", Area.Learning, "dome"),
    Patterns("patterns", "الأنماط", "نسخ الأنماط الهندسية", "patterns", Area.Learning, "triangle"),
    Balance("balance", "الميزان", "الميزان الحسابي البصري", "balance", Area.Learning, "squircle");

    companion object {
        fun byId(id: String) = entries.firstOrNull { it.id == id }
    }
}

/**
 * One hue per activity, kept on its stop, tile, badge and header (IR-UI-007). The palette has eight hues for nine
 * activities, so Balance repeats Environment's green; they sit at opposite ends of the road. Pictograms and labels
 * tell them apart, since colour is never the only cue (NFR-ACC-002).
 */
fun IdentityPalette.activityHue(activity: Activity): Hue = when (activity) {
    Activity.Emotions -> illusion
    Activity.Environment -> pastelGreen
    Activity.SocialPath -> downy
    Activity.Star -> texasRose
    Activity.Maze -> slateBlue
    Activity.Garden -> monaLisa
    Activity.Letters -> summerSky
    Activity.Patterns -> amethyst
    Activity.Balance -> pastelGreen
}
