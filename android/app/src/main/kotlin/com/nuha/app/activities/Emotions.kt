package com.nuha.app.activities

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.nuha.app.task.TrialSpec
import com.nuha.app.ui.identity.AvatarKinds
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.NuhaAvatar
import com.nuha.app.ui.identity.expressionFace
import com.nuha.app.ui.theme.learningText
import kotlin.random.Random

/**
 * «تلوين المشاعر» (FR-EMO-001/002). A shape buddy shows a feeling; the child picks the matching word from three or four
 * cards. Characters change across trials so the same feeling appears on different faces. Feelings are told apart by
 * face shape (brows, eyes, mouth), never by a colour standing for a feeling. Draft content (docs/activities/emotions.md).
 */
@DraftArabic
internal class Feeling(val id: String, val word: String, val cue: String)

@DraftArabic
internal val Feelings = listOf(
    Feeling("happy", "سَعِيد", "يبتسم."),
    Feeling("sad", "حَزِين", "فمه إلى الأسفل وحاجباه منخفضان."),
    Feeling("angry", "غَاضِب", "حاجباه مقطّبان."),
    Feeling("scared", "خَائِف", "عيناه واسعتان وفمه مفتوح."),
)

internal class EmotionItem(val feeling: Feeling, val character: String, val options: List<Feeling>)

/** Levels change one factor at a time (FR-ACT-012): the feelings in play, then the number of cards. */
internal class EmotionsPlan(completedSessions: Int) : ActivityPlan {
    private val level = Pending.levelFor(completedSessions, top = 2)
    private val pool = if (level == 0) 3 else 4
    private val options = if (level < 2) 3 else 4
    override val activity = Activity.Emotions
    override val taskVersion = 1
    override val difficulty = "level=$level;feelings=$pool;options=$options"
    override fun trials(seed: Long): List<TrialSpec> {
        val r = Random(seed)
        val feelings = Feelings.take(pool)
        val characters = AvatarKinds.shuffled(r)
        return List(slots) { i ->
            val target = feelings[(i + r.nextInt(feelings.size)) % feelings.size]
            val character = characters[i % characters.size]
            val choices = (listOf(target) + (feelings - target).shuffled(r).take(options - 1)).shuffled(r)
            TrialSpec(
                key = "${target.id}-$character",
                instruction = "كيف يشعر صديقنا؟",
                praise = "أحسنت! صديقنا ${target.word}، ${target.cue}",
                model = "صديقنا ${target.word}. ${target.cue} اضغط على «${target.word}».",
                item = EmotionItem(target, character, choices),
            )
        }
    }
}

@Composable
internal fun EmotionsContent(scope: TaskScope) {
    val c = LocalIdentity.current
    val hue = c.activityHue(Activity.Emotions)
    val item = scope.spec.item as EmotionItem
    Row(verticalAlignment = Alignment.CenterVertically) {
        // The buddy showing the feeling, on the right where reading starts.
        NuhaAvatar(item.character, Modifier.size(330.dp).semantics { contentDescription = "صديق" }, expression = item.feeling.id)
        Spacer(Modifier.width(56.dp))
        val rows = item.options.chunked(if (item.options.size > 3) 2 else 3)
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            for (row in rows) Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                for (option in row) AnswerTile(option.id, option == item.feeling, scope, hue, option.word, Modifier.size(width = 200.dp, height = if (rows.size > 1) 170.dp else 220.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FeelingGlyph(option.id, c.ink, Modifier.size(if (rows.size > 1) 64.dp else 88.dp))
                        Text(option.word, color = c.ink, style = learningText(34f))
                    }
                }
            }
        }
    }
}

/** A plain line face with the feeling's features, so each card shows the feeling on a neutral face. */
@Composable
internal fun FeelingGlyph(feeling: String, ink: Color, modifier: Modifier) {
    Canvas(modifier.clearAndSetSemantics {}) {
        val s = size.minDimension / 100f
        drawContext.canvas.save(); drawContext.canvas.scale(s, s)
        drawCircle(ink, 40f, androidx.compose.ui.geometry.Offset(50f, 52f), style = Stroke(4f))
        expressionFace(ink, 48f, feeling)
        drawContext.canvas.restore()
    }
}

