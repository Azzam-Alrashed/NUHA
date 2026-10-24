package com.nuha.app.child

import com.nuha.app.DraftArabic
import com.nuha.app.Numerals

/** Child-mode lines, shown and spoken. All draft until an Arabic early-years educator reviews them. */
@DraftArabic
object ChildCopy {
    fun mapGreeting(name: String) = "هيّا نلعب يا $name! اختر محطة."
    const val MapHint = "اضغط على المحطة المضيئة لنبدأ."
    const val Done = "انتهيت"
    fun goodbye(name: String) = "إلى اللقاء يا $name!"
    const val PlayAgain = "نلعب مرة أخرى"
    const val WhoPlays = "من يلعب الآن؟"
    const val Pause = "استراحة"
    const val Resume = "نكمل"
    const val Map = "الخريطة"
    const val StartHere = "نبدأ من هنا"
    fun stars(earned: Int, n: Numerals) = "${n.format(earned)} من ${n.format(3)} نجوم"

    // Task screen.
    const val Repeat = "أعد التعليمة"
    const val Help = "ساعدني"
    const val HintLine = "انظر إلى المكان المضيء."
    fun board(filled: Int, slots: Int, n: Numerals) = "${n.format(filled)} من ${n.format(slots)} نجوم"
    const val ResumeQuestion = "نكمل من حيث توقفنا؟"
    fun resumeSaved(saved: Int, slots: Int, n: Numerals) = "جمعت ${n.format(saved)} من ${n.format(slots)} نجوم."
    const val StartOver = "نبدأ من جديد"
    const val Undo = "تراجع"
    const val Clear = "امسح"

    // Session celebration (FR-RWD-003).
    const val Bravo = "أحسنت!"
    fun celebrate(name: String) = "أحسنت يا $name! ملأت كل النجوم."
    fun completed(filled: Int, slots: Int, n: Numerals) = "أكملت ${n.format(filled)} من ${n.format(slots)}"
}
