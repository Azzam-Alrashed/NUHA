package com.nuha.app.onboarding

import com.nuha.app.ui.identity.Hue
import com.nuha.app.ui.identity.IdentityPalette

/** The three skill areas the legacy app grouped its games into. */
enum class Area { Communication, Attention, Learning }

class Question(val text: String, val area: Area)

/**
 * The legacy app's ten parent observation questions (docs/legacy-app-reference.md, Child profile creation),
 * reworded so "yes" always means "this is harder for my child". Placeholder wording: it needs specialist and
 * native-speaker review before use with families.
 */
val Questions = listOf(
    Question("هل يصعب على طفلك الاستجابة عندما يُنادى باسمه أو يُكلَّم؟", Area.Communication),
    Question("هل يتجنّب طفلك النظر في العينين أثناء اللعب أو الحديث؟", Area.Communication),
    Question("هل تلاحظ تأخرًا في الكلام أو اللغة مقارنةً بأقرانه؟", Area.Communication),
    Question("هل يصعب عليه الانتباه لأنشطة تناسب عمره؟", Area.Attention),
    Question("هل تؤثّر حركته الكثيرة على أنشطته اليومية؟", Area.Attention),
    Question("هل يكرّر كلمات أو عبارات يسمعها من الآخرين؟", Area.Communication),
    Question("هل يتعلّق بلعبة واحدة بشكل يمنعه من استكشاف غيرها؟", Area.Communication),
    Question("هل يصعب عليه تعلّم الحروف أو الأرقام التي تدرّب عليها؟", Area.Learning),
    Question("هل ينزعج من العناق أو اللمس حتى عندما يكون هو من يطلبه؟", Area.Attention),
    Question("هل يتذوّق الأشياء ليتعرّف عليها؟", Area.Attention),
)

enum class Answer(val code: Char, val label: String, val weight: Int) {
    Yes('y', "نعم", 2), No('n', "لا", 0), Unsure('u', "غير متأكد", 1),
}

/** Where the road begins for a child. [pictogram] matches NuhaPictogram kinds; Mixed shows all three. */
enum class StartPath(val id: String, val label: String, val pictogram: String?, val reason: String) {
    Communication("communication", "التواصل والمشاعر", "emotions", "ألعاب للتعرّف على المشاعر والتواصل مع الآخرين."),
    Attention("attention", "الانتباه والحركة", "focus", "ألعاب قصيرة للتركيز وتنظيم الحركة بهدوء."),
    Learning("learning", "التعلّم المبكر", "letters", "ألعاب للحروف والأرقام والأنماط."),
    Mixed("mixed", "قليل من كل شيء", null, "لا يبرز مجال واحد بوضوح، فنبدأ بأنشطة متنوّعة تناسب العمر."),
}

fun IdentityPalette.pathHue(path: StartPath): Hue = when (path) {
    StartPath.Communication -> illusion
    StartPath.Attention -> texasRose
    StartPath.Learning -> summerSky
    StartPath.Mixed -> amethyst
}

/**
 * Scores each area as the share of its maximum (yes 2, unsure 1, no 0). An area leads when it reaches half its
 * maximum and is at least 0.15 ahead of the next; otherwise the start is Mixed, as the legacy app recommended
 * varied activities when indicators were insufficient. A starting suggestion only, never a diagnosis.
 */
fun startPathFor(answers: List<Answer?>): StartPath {
    val scores = Area.entries.associateWith { area ->
        val asked = Questions.indices.filter { Questions[it].area == area }
        asked.sumOf { answers.getOrNull(it)?.weight ?: 0 } / (2f * asked.size)
    }.entries.sortedByDescending { it.value }
    val (lead, next) = scores[0] to scores[1]
    if (lead.value < .5f || lead.value - next.value < .15f) return StartPath.Mixed
    return when (lead.key) {
        Area.Communication -> StartPath.Communication
        Area.Attention -> StartPath.Attention
        Area.Learning -> StartPath.Learning
    }
}
