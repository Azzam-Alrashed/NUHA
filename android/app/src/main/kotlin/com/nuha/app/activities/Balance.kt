package com.nuha.app.activities

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuha.app.DraftArabic
import com.nuha.app.Pending
import com.nuha.app.child.Activity
import com.nuha.app.child.activityHue
import com.nuha.app.task.ActivityPlan
import com.nuha.app.task.AnswerTile
import com.nuha.app.task.TaskScope
import com.nuha.app.task.TrialSpec
import com.nuha.app.ui.identity.Hue
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.scene.placeAt
import com.nuha.app.ui.theme.buttonWeight
import com.nuha.app.ui.theme.titleWeight
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * «الميزان الحسابي البصري» (FR-BAL-001). One pan holds two groups of dots; the other is empty. The child may tap each
 * group to hear its count before answering, then picks the number that balances the scale; when it does, the empty pan
 * fills with the same quantity and the beam levels. Answering by writing is PLACEHOLDER: it needs offline digit
 * recognition and the numeral decision (OI-02), so only selection is built and the record says so. Draft content.
 */
internal class BalanceItem(val a: Int, val b: Int, val options: List<Int>) { val sum get() = a + b }

/** Levels change one factor at a time (FR-ACT-012): sums to 5, then to 8, then four choices instead of three. */
internal class BalancePlan(completedSessions: Int) : ActivityPlan {
    private val level = Pending.levelFor(completedSessions, top = 2)
    private val maxSum = if (level == 0) 5 else 8
    private val choices = if (level < 2) 3 else 4
    override val activity = Activity.Balance
    override val taskVersion = 1
    override val difficulty = "level=$level;maxSum=$maxSum;choices=$choices;input=select"
    @DraftArabic
    override fun trials(seed: Long): List<TrialSpec> {
        val r = Random(seed)
        return List(slots) { i ->
            val sum = r.nextInt(2, maxSum + 1)
            val a = r.nextInt(1, sum)
            val b = sum - a
            val wrong = ((1..maxSum) - sum).shuffled(r).take(choices - 1)
            val options = (wrong + sum).shuffled(r)
            val (na, nb, ns) = listOf(a, b, sum).map(Pending::numeral)
            TrialSpec(
                "sum-$i-$a+$b", "كم نضع في الكفّة الفارغة ليتوازن الميزان؟", "أحسنت! $na و$nb يساوي $ns. الميزان متوازن.",
                "عُدّ النقاط: $na و$nb يساوي $ns. اضغط على $ns.", BalanceItem(a, b, options),
            )
        }
    }
}

@Composable
internal fun BalanceContent(scope: TaskScope) {
    val c = LocalIdentity.current
    val hue = c.activityHue(Activity.Balance)
    val item = scope.spec.item as BalanceItem
    // The full pan (right, where reading starts) sits low until the scale balances.
    val tilt by animateFloatAsState(if (scope.solved) 0f else 9f, if (scope.reducedMotion) snap() else spring(dampingRatio = .45f, stiffness = 120f), label = "Beam")
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth().height(360.dp), contentAlignment = AbsoluteAlignment.TopLeft) {
            val w = maxWidth
            val pivot = Offset(w.value / 2, 70f)
            val half = (w.value * .3f)
            val drop = (sin(tilt * PI / 180) * half).toFloat()
            Canvas(Modifier.fillMaxSize()) {
                val p = Offset(pivot.x.dp.toPx(), pivot.y.dp.toPx())
                val d = drop.dp.toPx(); val hx = half.dp.toPx()
                drawLine(c.ink.copy(alpha = .75f), p, Offset(p.x, size.height - 20.dp.toPx()), 10.dp.toPx(), StrokeCap.Round)
                drawRoundRect(c.ink.copy(alpha = .75f), Offset(p.x - 70.dp.toPx(), size.height - 26.dp.toPx()), androidx.compose.ui.geometry.Size(140.dp.toPx(), 16.dp.toPx()), CornerRadius(8.dp.toPx()))
                drawLine(hue.lip, Offset(p.x - hx, p.y - d), Offset(p.x + hx, p.y + d), 16.dp.toPx(), StrokeCap.Round)
                drawCircle(hue.fill, 18.dp.toPx(), p)
                for ((x, y) in listOf(p.x - hx to p.y - d, p.x + hx to p.y + d)) drawLine(c.ink.copy(alpha = .5f), Offset(x, y), Offset(x, y + 60.dp.toPx()), 3.dp.toPx())
            }
            // Pans hang from the beam's ends.
            Pan(Modifier.placeAt((pivot.x + half).dp, (pivot.y + drop + 60).dp, alignY = 0f), hue) {
                Group(item.a, hue, scope); Text("+", color = c.ink, style = MaterialTheme.typography.displaySmall.titleWeight()); Group(item.b, hue, scope)
            }
            Pan(Modifier.placeAt((pivot.x - half).dp, (pivot.y - drop + 60).dp, alignY = 0f), hue) {
                if (scope.solved) Group(item.sum, hue, scope)
                else Box(Modifier.size(96.dp).drawBehind {
                    drawRoundRect(hue.lip, style = Stroke(4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12.dp.toPx(), 8.dp.toPx()))), cornerRadius = CornerRadius(18.dp.toPx()))
                }, contentAlignment = Alignment.Center) { Text("؟", color = c.ink, style = MaterialTheme.typography.displaySmall.titleWeight()) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            for (n in item.options) AnswerTile("n$n", n == item.sum, scope, hue, Pending.numeral(n), Modifier.size(150.dp)) {
                Text(Pending.numeral(n), color = c.ink, style = MaterialTheme.typography.displaySmall.buttonWeight().copy(fontSize = 64.sp, lineHeight = 72.sp))
            }
        }
    }
}

@Composable
private fun Pan(modifier: Modifier, hue: Hue, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(bottomStart = 60.dp, bottomEnd = 60.dp, topStart = 16.dp, topEnd = 16.dp)
    Row(
        modifier.background(hue.tint, shape).border(3.dp, hue.lip, shape).padding(horizontal = 22.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) { content() }
}

/** A group of dots the child can tap to hear its count; inspecting is not an answer. */
@Composable
private fun Group(count: Int, hue: Hue, scope: TaskScope) {
    val say = { scope.say(Pending.numeral(count)) }
    Column(
        Modifier
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(10.dp)
            .semantics(mergeDescendants = true) { role = Role.Button; contentDescription = Pending.numeral(count); onClick { say(); true } }
            .pointerInput(count) { detectTapGestures(onPress = { say() }) },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (row in (1..count).chunked(4)) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(row.size) { Box(Modifier.size(32.dp).background(hue.fill, CircleShape).drawBehind { drawCircle(hue.lip, style = Stroke(3.dp.toPx())) }) }
        }
    }
}
