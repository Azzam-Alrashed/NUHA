package com.nuha.app.activities

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nuha.app.DraftArabic
import com.nuha.app.Pending
import com.nuha.app.child.Activity
import com.nuha.app.child.activityHue
import com.nuha.app.task.ActivityPlan
import com.nuha.app.task.AnswerTile
import com.nuha.app.task.TaskScope
import com.nuha.app.task.TracePad
import com.nuha.app.task.TrialSpec
import com.nuha.app.task.smoothGuide
import com.nuha.app.ui.identity.IdentityPalette
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.starPath
import com.nuha.app.ui.theme.learningText
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * «كتابة الحروف وتوصيل الكلمات» (FR-LET-001/002). Four separate task types, recorded in each trial's key: formation
 * (trace the letter), recognition (find the named letter), missing letter (complete a word under its picture) and
 * word–picture matching. Letters, words, letter guides and pictures are DRAFT pending an Arabic early-years educator
 * (OI-09): the letter set, the vowelisation, and above all the tracing guides, which are hand-made stand-ins for
 * educator-approved letter forms with stroke direction. Learning text is simplified Naskh (FR-ACT-015).
 */
@DraftArabic
internal class LetterWord(val letter: String, val name: String, val word: String, val rest: String, val picture: String)

@DraftArabic
internal val LetterWords = listOf(
    LetterWord("ب", "الباء", "بَيْت", "يْت", "house"),
    LetterWord("ش", "الشين", "شَمْس", "مْس", "sun"),
    LetterWord("ق", "القاف", "قَمَر", "مَر", "moon"),
    LetterWord("س", "السين", "سَمَكَة", "مَكَة", "fish"),
    LetterWord("ت", "التاء", "تُفَّاحَة", "فَّاحَة", "apple"),
    LetterWord("ن", "النون", "نَجْمَة", "جْمَة", "star"),
)

/** DRAFT tracing guides (0..1, square pad), each written right to left: the main stroke, then the dots shown as targets. */
internal class LetterGuide(val stroke: List<Offset>, val dots: List<Offset>)

internal val LetterGuides = mapOf(
    "ب" to LetterGuide(listOf(Offset(.84f, .36f), Offset(.8f, .56f), Offset(.62f, .66f), Offset(.4f, .66f), Offset(.22f, .58f), Offset(.16f, .4f)), listOf(Offset(.5f, .82f))),
    "ت" to LetterGuide(listOf(Offset(.84f, .4f), Offset(.8f, .6f), Offset(.62f, .7f), Offset(.4f, .7f), Offset(.22f, .62f), Offset(.16f, .44f)), listOf(Offset(.43f, .3f), Offset(.57f, .3f))),
    "ن" to LetterGuide(listOf(Offset(.74f, .38f), Offset(.76f, .56f), Offset(.64f, .72f), Offset(.5f, .76f), Offset(.36f, .72f), Offset(.25f, .56f), Offset(.27f, .38f)), listOf(Offset(.5f, .4f))),
)

internal class LetterItem(val type: String, val target: LetterWord, val options: List<LetterWord>)

/** Levels change one factor at a time (FR-ACT-012): the letters in play. The task types rotate within a session. */
internal class LettersPlan(completedSessions: Int) : ActivityPlan {
    private val level = Pending.levelFor(completedSessions, top = 2)
    private val pool = LetterWords.take(listOf(3, 5, 6)[level])
    override val activity = Activity.Letters
    override val taskVersion = 1
    override val difficulty = "level=$level;letters=${pool.joinToString("") { it.letter }}"
    @DraftArabic
    override fun trials(seed: Long): List<TrialSpec> {
        val r = Random(seed)
        val types = listOf("formation", "recognition", "missing", "word", "recognition")
        return types.mapIndexed { i, type ->
            val traceable = pool.filter { it.letter in LetterGuides }
            val target = if (type == "formation") traceable.random(r) else pool.random(r)
            val options = (listOf(target) + (pool - target).shuffled(r).take(2)).shuffled(r)
            val item = LetterItem(type, target, options)
            when (type) {
                "formation" -> TrialSpec("formation:${target.letter}-$i", "ارسم حرف ${target.name} فوق الخط.", "أحسنت! رسمت حرف ${target.name}.", "انظر، نبدأ من النقطة ونمشي فوق الخط. هذا حرف ${target.name}.", item)
                "recognition" -> TrialSpec("recognition:${target.letter}-$i", "اضغط على حرف ${target.name}.", "أحسنت! هذا حرف ${target.name}.", "هذا حرف ${target.name}. اضغط عليه.", item)
                "missing" -> TrialSpec("missing:${target.letter}-$i", "أيّ حرف ينقص في الكلمة؟", "أحسنت! ${target.word} تبدأ بحرف ${target.name}.", "${target.word} تبدأ بحرف ${target.name}. اضغط عليه.", item)
                else -> TrialSpec("word:${target.letter}-$i", "أيّ كلمة تناسب الصورة؟", "أحسنت! هذه ${target.word}.", "هذه ${target.word}. اضغط عليها.", item)
            }
        }
    }
}

@Composable
internal fun LettersContent(scope: TaskScope) {
    val c = LocalIdentity.current
    val hue = c.activityHue(Activity.Letters)
    val item = scope.spec.item as LetterItem
    when (item.type) {
        "formation" -> {
            val guide = LetterGuides.getValue(item.target.letter)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // The letter as printed, for reference; tracing happens on the guide beside it.
                Box(Modifier.size(220.dp).background(hue.tint, RoundedCornerShape(40.dp)), contentAlignment = Alignment.Center) {
                    Text(item.target.letter, color = c.ink, style = learningText(120f))
                }
                Spacer(Modifier.width(40.dp))
                TracePad(smoothGuide(guide.stroke), scope, hue, Modifier.fillMaxHeight().aspectRatio(1f), choice = "formation") { toPx ->
                    for (d in guide.dots) { drawCircle(hue.lip, 14.dp.toPx(), toPx(d)) }
                }
            }
        }
        "recognition" -> Row(horizontalArrangement = Arrangement.spacedBy(48.dp)) {
            for (o in item.options) AnswerTile(o.letter, o == item.target, scope, hue, o.name, Modifier.size(220.dp)) {
                Text(o.letter, color = c.ink, style = learningText(110f))
            }
        }
        "missing" -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(36.dp)) {
                WordPicture(item.target.picture, c, Modifier.size(170.dp))
                // The word with its first letter left as a box; a zero-width joiner keeps the rest in its joined form.
                Row(Modifier.semantics(mergeDescendants = true) { contentDescription = item.target.word }, verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(width = 84.dp, height = 104.dp).dashedBox(hue.lip))
                    Text("‍" + item.target.rest, color = c.ink, style = learningText(80f))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                for (o in item.options) AnswerTile(o.letter, o == item.target, scope, hue, o.name, Modifier.size(170.dp)) {
                    Text(o.letter, color = c.ink, style = learningText(84f))
                }
            }
        }
        else -> Row(verticalAlignment = Alignment.CenterVertically) {
            WordPicture(item.target.picture, c, Modifier.size(280.dp).semantics { contentDescription = "صورة" })
            Spacer(Modifier.width(56.dp))
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                for (o in item.options) AnswerTile(o.letter, o == item.target, scope, hue, o.word, Modifier.size(width = 300.dp, height = 112.dp)) {
                    Text(o.word, color = c.ink, style = learningText(48f))
                }
            }
        }
    }
}

/** A dashed rounded box marking the missing letter's place. */
private fun Modifier.dashedBox(color: Color) = drawBehind {
    drawRoundRect(color, style = Stroke(4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12.dp.toPx(), 8.dp.toPx()))), cornerRadius = CornerRadius(16.dp.toPx()))
}

/** Flat drawings for the words, in palette hues; DRAFT until the illustration set is decided. */
@Composable
internal fun WordPicture(kind: String, c: IdentityPalette, modifier: Modifier) {
    Canvas(modifier.clearAndSetSemantics {}) {
        val s = size.minDimension / 100f
        drawContext.canvas.save(); drawContext.canvas.scale(s, s)
        when (kind) {
            "house" -> {
                drawRect(c.summerSky.fill, Offset(22f, 46f), Size(56f, 44f))
                drawPath(Path().apply { moveTo(14f, 50f); lineTo(50f, 16f); lineTo(86f, 50f); close() }, c.monaLisa.fill)
                drawRoundRect(c.texasRose.fill, Offset(43f, 64f), Size(14f, 26f), CornerRadius(3f))
            }
            "sun" -> {
                drawCircle(c.texasRose.fill, 24f, Offset(50f, 50f))
                for (i in 0 until 8) { val a = i * PI / 4; drawLine(c.texasRose.lip, Offset(50f + (32 * cos(a)).toFloat(), 50f + (32 * sin(a)).toFloat()), Offset(50f + (42 * cos(a)).toFloat(), 50f + (42 * sin(a)).toFloat()), 6f, StrokeCap.Round) }
            }
            "moon" -> {
                val outer = Path().apply { addOval(Rect(Offset(50f, 50f), 36f)) }
                val inner = Path().apply { addOval(Rect(Offset(64f, 40f), 32f)) }
                drawPath(Path.combine(PathOperation.Difference, outer, inner), c.slateBlue.fill)
            }
            "fish" -> {
                drawPath(Path().apply { moveTo(26f, 50f); lineTo(8f, 34f); lineTo(8f, 66f); close() }, c.summerSky.lip)
                drawOval(c.summerSky.fill, Offset(20f, 32f), Size(64f, 36f))
                drawCircle(c.ink, 4f, Offset(70f, 46f))
            }
            "apple" -> {
                drawCircle(c.monaLisa.fill, 32f, Offset(50f, 58f))
                drawLine(c.texasRose.lip, Offset(50f, 30f), Offset(52f, 16f), 5f, StrokeCap.Round)
                drawOval(c.pastelGreen.fill, Offset(54f, 14f), Size(22f, 12f))
            }
            else -> drawPath(starPath(Offset(50f, 52f), 40f), c.texasRose.fill)
        }
        drawContext.canvas.restore()
    }
}
