package com.nuha.app.activities

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.nuha.app.DraftArabic
import com.nuha.app.Pending
import com.nuha.app.child.Activity
import com.nuha.app.child.ChildCopy
import com.nuha.app.child.RoundIconButton
import com.nuha.app.child.activityHue
import com.nuha.app.task.ActivityPlan
import com.nuha.app.task.InkSurface
import com.nuha.app.task.InputType
import com.nuha.app.task.Ladder
import com.nuha.app.task.SavedStroke
import com.nuha.app.task.StrokeCodec
import com.nuha.app.task.TaskScope
import com.nuha.app.task.TrialSpec
import com.nuha.app.ui.identity.ChunkyButton
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.identity.starPath
import com.nuha.app.ui.theme.buttonWeight
import kotlin.random.Random
import kotlinx.coroutines.launch

/**
 * «متاهة توازن الكرة» (FR-MAZ-001/002). Mechanic is PLACEHOLDER OI-10 ([Pending.MazeMechanic] = "route"): the child draws
 * the ball's route from the ball to the star, then presses «شغّل الكرة»; the ball rolls along the drawing in a
 * deterministic, replayable simulation and falls into a hole the route crosses. The record keeps the drawing (invalid
 * routes, attempts) apart from the simulation outcome (falls, as misses). Draft content (docs/activities/maze.md).
 */
internal class MazeItem(val holes: List<Circle>)

@DraftArabic private const val RunBall = "شغّل الكرة"
@DraftArabic private const val DrawFromBall = "ارسم طريقًا يبدأ من الكرة وينتهي عند النجمة."

private val Ball = Offset(.88f, .5f)
private val Goal = Offset(.12f, .5f)

/** Levels change one factor at a time (FR-ACT-012): the number of holes. */
internal class MazePlan(completedSessions: Int) : ActivityPlan {
    private val level = Pending.levelFor(completedSessions, top = 2)
    private val holes = level + 1
    override val activity = Activity.Maze
    override val taskVersion = 1
    override val difficulty = "level=$level;holes=$holes;mechanic=${Pending.MazeMechanic}"
    @DraftArabic
    override fun trials(seed: Long): List<TrialSpec> {
        val r = Random(seed)
        return List(slots) { i ->
            val xs = List(holes) { k -> .7f - .4f * (k + .5f) / holes }
            val placed = xs.map { x -> Circle(x, .5f + (r.nextFloat() - .5f) * .3f, .13f) }
            TrialSpec(
                "maze-$i", "ارسم طريق الكرة إلى النجمة، ثم شغّل الكرة.", "أحسنت! وصلت الكرة إلى النجمة.",
                "انظر، نرسم طريقًا يمرّ بجانب الحفرة ولا يدخلها.", MazeItem(placed),
            )
        }
    }
}

@Composable
internal fun MazeContent(scope: TaskScope) {
    val c = LocalIdentity.current
    val hue = c.activityHue(Activity.Maze)
    val item = scope.spec.item as MazeItem
    val strokes = remember(scope.trialNumber) { mutableStateListOf<SavedStroke>() }
    var size by remember { mutableStateOf(IntSize.Zero) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var falls by remember(scope.trialNumber) { mutableIntStateOf(0) }
    var invalid by remember(scope.trialNumber) { mutableIntStateOf(0) }
    var rolling by remember(scope.trialNumber) { mutableStateOf(false) }
    var fellIn by remember(scope.trialNumber) { mutableStateOf<Int?>(null) }
    val roll = remember(scope.trialNumber) { Animatable(0f) }
    val launch = rememberCoroutineScope()
    LaunchedEffect(scope.wrongTick) { if (scope.wrongTick > 0) { kotlinx.coroutines.delay(700); strokes.clear(); roll.snapTo(0f); fellIn = null } }
    val route = strokes.lastOrNull()?.points.orEmpty()
    val latest by rememberUpdatedState(scope)

    // Reads the drawing and the scope when pressed, not when composed: the button keeps the handler it was given.
    val run: () -> Unit = run@{
        val now = latest
        val drawn = strokes.lastOrNull()?.points.orEmpty()
        val aspect = if (size.height == 0) 1f else size.width / size.height.toFloat()
        if (rolling || now.solved || drawn.size < 2) return@run
        val check = checkRoute(drawn, Ball.x, Ball.y, Goal.x, Goal.y, emptyList(), aspect)
        if (!check.connects) { invalid++; now.say(DrawFromBall); return@run }
        val fall = rollBall(drawn, item.holes, aspect)
        rolling = true
        launch.launch {
            val stopAt = fall?.second ?: drawn.lastIndex
            if (!now.reducedMotion) roll.animateTo(stopAt / drawn.lastIndex.toFloat(), tween((stopAt * 18).coerceIn(400, 2400), easing = LinearEasing))
            else roll.snapTo(stopAt / drawn.lastIndex.toFloat())
            val tool = strokes.lastOrNull()?.tool ?: InputType.Other
            val end = drawn[stopAt].let { origin + Offset(it.x * size.width, it.y * size.height) }
            if (fall == null) now.respond("roll", true, tool, end, StrokeCodec.encode(strokes), "falls=$falls;invalidRoutes=$invalid;mechanic=${Pending.MazeMechanic}")
            else { fellIn = fall.first; falls++; now.miss(); now.respond("roll", false, tool, null, StrokeCodec.encode(strokes)) }
            rolling = false
        }
    }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(hue.tint, RoundedCornerShape(40.dp))
                .border(6.dp, hue.lip.copy(alpha = .5f), RoundedCornerShape(40.dp))
                .onGloballyPositioned { size = it.size; origin = it.positionInRoot() }
                .semantics { contentDescription = scope.spec.instruction },
            contentAlignment = AbsoluteAlignment.TopLeft,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                fun px(o: Offset) = Offset(o.x * size.width, o.y * size.height)
                val unit = minOf(size.width, size.height).toFloat()
                item.holes.forEachIndexed { i, hole ->
                    val center = px(Offset(hole.x, hole.y))
                    drawCircle(hue.lip.copy(alpha = .45f), hole.r * unit * 1.12f, center)
                    drawCircle(c.ink.copy(alpha = if (i == fellIn) .95f else .8f), hole.r * unit, center)
                    // At the hint level each hole gets a ring showing the space to keep clear.
                    if (scope.level >= Ladder.Hint) drawCircle(hue.lip, hole.r * unit * 1.3f, center, style = Stroke(3.dp.toPx()))
                }
                drawCircle(Color.White, 62.dp.toPx(), px(Goal))
                val goal = starPath(px(Goal), 46.dp.toPx())
                drawPath(goal, c.texasRose.fill); drawPath(goal, c.texasRose.lip, style = Stroke(4.dp.toPx(), join = StrokeJoin.Round))
                // A ball that fell sits small in the middle of its hole until the route is cleared.
                val fell = fellIn?.takeIf { !rolling }?.let { item.holes[it] }
                val ballAt = when {
                    fell != null -> px(Offset(fell.x, fell.y))
                    route.size > 1 && (rolling || roll.value > 0f) -> route[(roll.value * route.lastIndex).toInt()].let { px(Offset(it.x, it.y)) }
                    else -> px(Ball)
                }
                val shrink = if (fell != null) .4f else 1f
                drawCircle(hue.lip, 36.dp.toPx() * shrink, ballAt + Offset(0f, 5.dp.toPx()))
                drawCircle(hue.fill, 36.dp.toPx() * shrink, ballAt)
                drawCircle(Color.White.copy(alpha = .55f), 10.dp.toPx() * shrink, ballAt + Offset(-11.dp.toPx(), -11.dp.toPx()))
            }
            InkSurface(strokes, c.ink.copy(alpha = .8f), widthDp = 8f, enabled = !scope.solved && !rolling, onStroke = { s ->
                // One route at a time: a new line replaces the old one.
                strokes.clear(); strokes += s; scope.active()
            }, modifier = Modifier.fillMaxSize())
            Box(Modifier.padding(16.dp)) { RoundIconButton("clear", ChildCopy.Clear, c.action, null) { if (!rolling && !scope.solved) strokes.clear() } }
        }
        // Below the board, so no route has to pass under it.
        ChunkyButton(
            RunBall, run, Modifier.height(96.dp).widthIn(min = 300.dp),
            enabled = route.size > 1 && !rolling && !scope.solved, style = MaterialTheme.typography.headlineMedium.buttonWeight(),
        )
    }
}
