package com.nuha.app.adult

import com.nuha.app.DraftArabic

/** Adult-area wording. Not child content, but unreviewed Arabic all the same. */
@DraftArabic
internal object AdultCopy {
    const val Entry = "للكبار"
    const val EntryDescription = "للكبار. اضغط مطولًا لفتح منطقة الكبار."
    const val EntryAction = "فتح منطقة الكبار"
    const val GateTitle = "منطقة الكبار"
    const val GatePrompt = "اكتب هذا العدد بالأرقام:"
    const val GateNote = "هذه المنطقة لإدارة ملفات الأطفال وإعداداتهم."
    const val DeleteDigit = "حذف آخر رقم"
    const val Delete = "حذف"
    const val Cancel = "إلغاء"
    const val Back = "رجوع"
    const val Save = "حفظ"
    const val Home = "منطقة الكبار"
    const val BackToPlay = "العودة إلى اللعب"
    const val Children = "الأطفال"
    const val AddChild = "إضافة طفل"
    const val AddChildNote = "يمرّ الطفل الجديد بخطوات البداية نفسها، ولا تتغيّر الملفات الحالية."
    const val Play = "العب الآن"
    const val Profile = "الملف"
    const val Settings = "الإعدادات"
    fun age(years: String) = "العمر: $years"
    fun profileTitle(name: String) = "ملف $name"
    fun settingsTitle(name: String) = "إعدادات $name"
    const val Name = "الاسم"
    fun nameHelp(max: String) = "من حرف إلى $max حرفًا"
    const val Age = "العمر"
    const val Avatar = "الرفيق"
    const val DeleteChild = "حذف الملف"
    fun deleteTitle(name: String) = "حذف ملف $name؟"
    const val DeleteBody = "سيُحذف الملف وإعداداته وكل ما سُجّل له من جلسات ومحاولات. لا يمكن التراجع عن ذلك."
    const val Appearance = "المظهر"
    val AppearanceChoices = listOf("colourful" to "ملوّن", "muted" to "هادئ", "contrast" to "تباين عالٍ")
    const val Motion = "الحركة"
    val MotionChoices = listOf("full" to "كاملة", "reduced" to "مخففة", "none" to "بلا حركة")
    const val Sound = "الصوت"
    const val Voice = "الصوت المنطوق"
    const val Effects = "المؤثرات الصوتية"
    const val Music = "الموسيقى"
    const val MusicNote = "لا توجد موسيقى في التطبيق بعد."
    const val Celebration = "الاحتفال"
    val CelebrationChoices = listOf("off" to "بلا احتفال", "gentle" to "هادئ", "lively" to "حيوي")
    const val Mascot = "الرفيق في المهام"
    val MascotChoices = listOf("activity" to "رفيق النشاط", "guide" to "المرشدة", "off" to "بلا رفيق")
    const val PromptDelay = "مهلة المساعدة التلقائية"
    fun seconds(n: String) = "$n ث"
    const val Never = "أبدًا"
    const val TargetSize = "حجم الأهداف"
    val TargetChoices = listOf("standard" to "عادي", "large" to "كبير")
    const val Numerals = "الأرقام"
    const val MapLock = "قفل الخريطة"
    const val MapLockNote = "عند القفل لا يرى الطفل إلا محطة واحدة."
    const val WholeMap = "كل المحطات"
    const val AppliesNote = "تُحفظ التغييرات فورًا وتنطبق على هذا الطفل فقط."
    const val Sensory = "نقطة البداية الحسية"
    const val SensoryPrompt = "اختر كيف يبدأ هذا الطفل. يمكن تغيير كل شيء لاحقًا من الإعدادات."
    val SensoryChoices = listOf(
        Triple("lively", "حيوية", "ألوان كاملة، حركة كاملة، احتفال حيوي."),
        Triple("gentle", "هادئة", "ألوان كاملة، حركة مخففة، احتفال هادئ."),
        Triple("muted", "خافتة", "ألوان هادئة، بلا حركة، بلا مؤثرات ولا احتفال."),
    )
    const val SaveChild = "حفظ الطفل"
    const val SaveError = "تعذّر الحفظ. حاول مرة أخرى."
}
