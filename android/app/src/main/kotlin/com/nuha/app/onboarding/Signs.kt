package com.nuha.app.onboarding

import com.nuha.app.DraftArabic
import com.nuha.app.Pending

/** Conditions whose commonly associated signs the result screen names. Never shown as a diagnosis. */
@DraftArabic
enum class Condition(val label: String) {
    Autism("اضطراب طيف التوحد"),
    Adhd("فرط الحركة وتشتت الانتباه"),
    LearningDifficulty("صعوبات التعلّم"),
}

/**
 * One sign of [condition], read from the observation question at [question] (0-based). It counts when the answer
 * is at least [atLeast]: Yes counts only yes; Unsure stands in for the owner's "sometimes" and counts unsure or yes.
 */
class Sign(val condition: Condition, val question: Int, val label: String, val atLeast: Answer)

/**
 * PLACEHOLDER (owner's sign list, 2026-10-25; needs specialist review): which answers count as a sign of which
 * condition. Two signs on the owner's list have no question yet and are left out: lining up toys (autism, yes) and
 * screaming (autism, sometimes, asked here only as movement).
 */
@DraftArabic
val Signs = listOf(
    Sign(Condition.Autism, 1, "قلة التواصل البصري", Answer.Yes),
    Sign(Condition.Autism, 0, "صعوبة الاستجابة", Answer.Yes),
    Sign(Condition.Autism, 2, "تأخر اللغة", Answer.Unsure),
    Sign(Condition.Autism, 4, "فرط الحركة", Answer.Unsure),
    Sign(Condition.Autism, 5, "ترديد الكلام", Answer.Unsure),
    Sign(Condition.Autism, 6, "التعلّق بلعبة واحدة", Answer.Yes),
    Sign(Condition.Autism, 8, "عدم تقبّل التلامس", Answer.Yes),
    Sign(Condition.Autism, 9, "التعرّف على الأشياء بالتذوّق", Answer.Unsure),
    Sign(Condition.Adhd, 7, "بطء التعلّم", Answer.Unsure),
    Sign(Condition.Adhd, 3, "ضعف الانتباه", Answer.Yes),
    Sign(Condition.Adhd, 4, "فرط الحركة", Answer.Yes),
    Sign(Condition.LearningDifficulty, 7, "صعوبة في الحروف والأرقام", Answer.Yes),
)

/** A condition whose signs the answers show: [matched] of its [total] signs. */
class SignMatch(val condition: Condition, val matched: Int, val total: Int)

/**
 * The conditions where the answers show at least [Pending.ConditionSignsMin] of the signs, in [Condition] order.
 * Signs commonly associated with a condition, never a diagnosis.
 */
fun signsFor(answers: List<Answer?>): List<SignMatch> = Condition.entries.mapNotNull { condition ->
    val signs = Signs.filter { it.condition == condition }
    val matched = signs.count { sign -> answers.getOrNull(sign.question)?.let { it.weight >= sign.atLeast.weight } == true }
    if (matched > 0 && matched >= signs.size * Pending.ConditionSignsMin) SignMatch(condition, matched, signs.size) else null
}
