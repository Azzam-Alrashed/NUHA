package com.nuha.app.activities

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.nuha.app.DraftArabic
import com.nuha.app.Pending
import com.nuha.app.child.Activity
import com.nuha.app.child.activityHue
import com.nuha.app.task.ActivityPlan
import com.nuha.app.task.AnswerTile
import com.nuha.app.task.InputType
import com.nuha.app.task.TaskScope
import com.nuha.app.task.TrialSpec
import com.nuha.app.ui.identity.IdentityPalette
import com.nuha.app.ui.identity.LocalIdentity
import com.nuha.app.ui.theme.titleWeight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.launch

/**
 * «رحلة مطابقة البيئة» (FR-ENV-001/002). One item per trial; the child puts it in its environment by dragging it there or,
 * as the tap alternative (FR-ACT-005), tapping the environment with the item already picked up. Each item has a defined
 * set of accepted environments. A drop that lands on no environment returns the item and is recorded as a miss, apart
 * from a wrong environment (a conceptual choice). Draft content (docs/activities/environment.md).
 */
@DraftArabic
internal class Place(val id: String, val name: String)

@DraftArabic
internal val Places = listOf(Place("rain", "المطر"), Place("sea", "البحر"), Place("garden", "الحديقة"))

@DraftArabic
internal class Thing(val id: String, val name: String, val accepted: Set<String>)

@DraftArabic
internal val Things = listOf(
    Thing("umbrella", "المظلّة", setOf("rain")), Thing("boots", "حذاء المطر", setOf("rain")),
    Thing("fish", "السمكة", setOf("sea")), Thing("boat", "القارب", setOf("sea")),
    Thing("flower", "الوردة", setOf("garden")), Thing("butterfly", "الفراشة", setOf("garden")),
)

internal class PlaceItem(val thing: Thing, val places: List<Place>)

/** Levels change one factor at a time (FR-ACT-012): two environments, then three. */
internal class EnvironmentPlan(completedSessions: Int) : ActivityPlan {
    private val level = Pending.levelFor(completedSessions, top = 1)
    private val places = Places.take(if (level == 0) 2 else 3)
    override val activity = Activity.Environment
    override val taskVersion = 1
    override val difficulty = "level=$level;places=${places.size}"
    @DraftArabic
    override fun trials(seed: Long): List<TrialSpec> {
        val r = Random(seed)
        val ids = places.map { it.id }.toSet()
        val pool = Things.filter { it.accepted.any(ids::contains) }.shuffled(r)
        return List(slots) { i ->
            val thing = pool[i % pool.size]
            val place = places.first { it.id in thing.accepted }
            TrialSpec(
                "${thing.id}-$i", "أين مكان ${thing.name}؟", "أحسنت! مكان ${thing.name} هو ${place.name}.",
                "مكان ${thing.name} هو ${place.name}. ضع ${thing.name} هناك.", PlaceItem(thing, places),
            )
        }
    }
}

@Composable
internal fun EnvironmentContent(scope: TaskScope) {
    val c = LocalIdentity.current
    val hue = c.activityHue(Activity.Environment)
    val item = scope.spec.item as PlaceItem
    val zones = remember(scope.trialNumber) { mutableStateMapOf<String, Rect>() }
    val drag = remember(scope.trialNumber) { Animatable(Offset.Zero, Offset.VectorConverter) }
    var home by remember { mutableStateOf(Offset.Zero) }
    val launch = rememberCoroutineScope()
    fun answer(place: Place, input: InputType, at: Offset?) = scope.respond(place.id, place.id in item.thing.accepted, input, at)
    // Once solved, by dragging or tapping, the item settles in the middle of its environment.
    LaunchedEffect(scope.solved, scope.trialNumber) {
        val target = item.places.firstOrNull { it.id in item.thing.accepted }?.let { zones[it.id] }
        if (scope.solved && target != null) {
            val to = target.center - home
            if (scope.reducedMotion) drag.snapTo(to) else drag.animateTo(to, spring(dampingRatio = .7f, stiffness = 300f))
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(36.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(36.dp)) {
            for (place in item.places) AnswerTile(
                place.id, place.id in item.thing.accepted, scope, hue, place.name,
                Modifier.size(width = 330.dp, height = 300.dp).onGloballyPositioned { zones[place.id] = it.boundsInRoot() },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // The scene fades back while the item sits on it, so the item reads clearly.
                    PlaceScene(place.id, c, Modifier.size(200.dp).graphicsLayer { alpha = if (scope.solved && place.id in item.thing.accepted) .35f else 1f })
                    Text(place.name, color = c.ink, style = MaterialTheme.typography.headlineMedium.titleWeight())
                }
            }
        }
        // The item, already picked up, on its own pad: drag it to an environment, or tap the environment.
        Box(Modifier.size(width = 300.dp, height = 210.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(width = 300.dp, height = 70.dp).align(Alignment.BottomCenter).background(hue.tint, RoundedCornerShape(50)))
            Box(
                Modifier
                    .size(190.dp)
                    .zIndex(1f)
                    .onGloballyPositioned { if (drag.value == Offset.Zero) home = it.positionInRoot() + Offset(it.size.width / 2f, it.size.height / 2f) }
                    .graphicsLayer { translationX = drag.value.x; translationY = drag.value.y }
                    .semantics { contentDescription = item.thing.name }
                    .pointerInput(scope.trialNumber) {
                        detectDragGestures(
                            onDrag = { change, delta -> change.consume(); scope.active(); launch.launch { drag.snapTo(drag.value + delta) } },
                            onDragEnd = {
                                val at = home + drag.value
                                val hit = item.places.firstOrNull { zones[it.id]?.contains(at) == true }
                                if (hit != null) answer(hit, InputType.Finger, at) else scope.miss()
                                if (hit == null || hit.id !in item.thing.accepted) launch.launch { drag.animateTo(Offset.Zero, spring(dampingRatio = .6f)) }
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) { ThingPicture(item.thing.id, c, Modifier.fillMaxSize()) }
        }
    }
}

/** Flat scenes for the environments; DRAFT until the illustration set is decided. */
@Composable
internal fun PlaceScene(kind: String, c: IdentityPalette, modifier: Modifier) {
    Canvas(modifier.clearAndSetSemantics {}) {
        val s = size.minDimension / 100f
        drawContext.canvas.save(); drawContext.canvas.scale(s, s)
        when (kind) {
            "rain" -> {
                // Outline first, then the fill over its inner half, so the cloud keeps an edge when tints turn white.
                val edge = Stroke(6f)
                drawCircle(c.slateBlue.lip, 18f, Offset(36f, 38f), style = edge); drawCircle(c.slateBlue.lip, 24f, Offset(58f, 32f), style = edge)
                drawRoundRect(c.slateBlue.lip, Offset(18f, 34f), Size(66f, 22f), CornerRadius(11f), style = edge)
                drawCircle(c.slateBlue.tint, 18f, Offset(36f, 38f)); drawCircle(c.slateBlue.tint, 24f, Offset(58f, 32f)); drawRoundRect(c.slateBlue.tint, Offset(18f, 34f), Size(66f, 22f), CornerRadius(11f))
                for ((x, y) in listOf(28f to 70f, 46f to 78f, 64f to 70f, 38f to 90f, 58f to 92f)) drawLine(c.summerSky.fill, Offset(x, y), Offset(x - 4f, y + 8f), 4f, StrokeCap.Round)
            }
            "sea" -> {
                drawRect(c.summerSky.tint, Offset(6f, 30f), Size(88f, 64f))
                for (y in listOf(42f, 60f, 78f)) drawPath(Path().apply { moveTo(8f, y); var x = 8f; while (x < 92f) { quadraticTo(x + 7f, y - 7f, x + 14f, y); x += 14f } }, c.summerSky.fill, style = Stroke(4f, cap = StrokeCap.Round))
            }
            else -> {
                drawRect(c.pastelGreen.tint, Offset(6f, 60f), Size(88f, 34f))
                drawLine(c.pastelGreen.lip, Offset(50f, 60f), Offset(50f, 36f), 4f)
                for (i in 0 until 5) { val a = i * 2 * PI / 5; drawCircle(c.illusion.fill, 8f, Offset(50f + (10 * cos(a)).toFloat(), 28f + (10 * sin(a)).toFloat())) }
                drawCircle(c.texasRose.fill, 6f, Offset(50f, 28f))
            }
        }
        drawContext.canvas.restore()
    }
}

/** Flat drawings of the items; DRAFT until the illustration set is decided. */
@Composable
internal fun ThingPicture(kind: String, c: IdentityPalette, modifier: Modifier) {
    Canvas(modifier.clearAndSetSemantics {}) {
        val s = size.minDimension / 100f
        drawContext.canvas.save(); drawContext.canvas.scale(s, s)
        when (kind) {
            "umbrella" -> {
                drawArc(c.monaLisa.fill, 180f, 180f, true, Offset(14f, 18f), Size(72f, 56f))
                drawLine(c.ink, Offset(50f, 46f), Offset(50f, 82f), 5f, StrokeCap.Round)
                drawArc(c.ink, 0f, 180f, false, Offset(38f, 74f), Size(12f, 12f), style = Stroke(5f, cap = StrokeCap.Round))
            }
            "boots" -> {
                drawPath(Path().apply { moveTo(30f, 20f); lineTo(52f, 20f); lineTo(52f, 66f); lineTo(80f, 66f); lineTo(80f, 84f); lineTo(30f, 84f); close() }, c.texasRose.fill)
                drawRect(c.texasRose.lip, Offset(30f, 78f), Size(50f, 6f))
            }
            "fish" -> {
                drawPath(Path().apply { moveTo(26f, 50f); lineTo(8f, 34f); lineTo(8f, 66f); close() }, c.summerSky.lip)
                drawOval(c.summerSky.fill, Offset(20f, 32f), Size(64f, 36f)); drawCircle(c.ink, 4f, Offset(70f, 46f))
            }
            "boat" -> {
                drawPath(Path().apply { moveTo(14f, 62f); lineTo(86f, 62f); lineTo(72f, 82f); lineTo(28f, 82f); close() }, c.texasRose.lip)
                drawLine(c.ink, Offset(50f, 62f), Offset(50f, 16f), 4f)
                val sail = Path().apply { moveTo(52f, 18f); lineTo(80f, 56f); lineTo(52f, 56f); close() }
                drawPath(sail, c.summerSky.tint); drawPath(sail, c.summerSky.lip, style = Stroke(3f, join = StrokeJoin.Round))
            }
            "flower" -> {
                drawLine(c.pastelGreen.lip, Offset(50f, 50f), Offset(50f, 90f), 5f)
                for (i in 0 until 5) { val a = i * 2 * PI / 5; drawCircle(c.illusion.fill, 13f, Offset(50f + (16 * cos(a)).toFloat(), 38f + (16 * sin(a)).toFloat())) }
                drawCircle(c.texasRose.fill, 9f, Offset(50f, 38f))
            }
            else -> {
                drawOval(c.amethyst.fill, Offset(18f, 22f), Size(30f, 34f)); drawOval(c.amethyst.fill, Offset(52f, 22f), Size(30f, 34f))
                drawOval(c.illusion.fill, Offset(24f, 52f), Size(24f, 26f)); drawOval(c.illusion.fill, Offset(52f, 52f), Size(24f, 26f))
                drawRoundRect(c.ink, Offset(46f, 24f), Size(8f, 56f), CornerRadius(4f))
            }
        }
        drawContext.canvas.restore()
    }
}

