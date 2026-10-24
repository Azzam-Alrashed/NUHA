package com.nuha.app.task

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nuha.app.ui.identity.CheckBadge
import com.nuha.app.ui.identity.ChunkySurface
import com.nuha.app.ui.identity.Hue
import com.nuha.app.ui.identity.pressSink

/** Maps a pointer to the input type recorded with a trial (IR-HW-001). */
fun PointerType.toInput(): InputType = when (this) {
    PointerType.Stylus, PointerType.Eraser -> InputType.Stylus
    PointerType.Touch -> InputType.Finger
    else -> InputType.Other
}

/**
 * An answer target for tap tasks. It fires on touch-down (FR-ACT-006) and never moves (FR-ACT-004). A wrong answer
 * shakes gently back in place, with no buzzer (FR-ACT-007). At the hint level the correct tile is ringed; at the model
 * level the ring pulses larger. A solved tile turns to its tint with a check badge, so success is not colour alone.
 */
@Composable
fun AnswerTile(key: String, correct: Boolean, scope: TaskScope, hue: Hue, description: String, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val solved = scope.solvedKey == key
    val hint = correct && !scope.solved && scope.level >= Ladder.Hint
    val model = correct && !scope.solved && scope.level >= Ladder.Model
    var pressed by remember { mutableStateOf(false) }
    var center by remember { mutableStateOf<Offset?>(null) }
    val respond by rememberUpdatedState { input: InputType -> scope.respond(key, correct, input, center) }
    val shake = remember { Animatable(0f) }
    LaunchedEffect(scope.wrongTick) {
        if (scope.wrongKey == key && scope.wrongTick > 0 && !scope.reducedMotion) shake.animateTo(0f, keyframes { durationMillis = 380; -14f at 70; 12f at 160; -7f at 250; 0f at 380 })
    }
    val pulse = if (hint && !scope.reducedMotion) {
        rememberInfiniteTransition(label = "Hint").animateFloat(0f, 1f, infiniteRepeatable(tween(if (model) 600 else 1000), RepeatMode.Reverse), label = "Ring").value
    } else 0f
    val lip = if (solved) 9.dp else 6.dp
    Box(
        modifier
            .graphicsLayer { translationX = shake.value * density }
            .onGloballyPositioned { center = it.positionInRoot() + Offset(it.size.width / 2f, it.size.height / 2f) }
            .semantics(mergeDescendants = true) {
                role = Role.Button; contentDescription = description; selected = solved
                onClick { respond(InputType.Other); true }
            }
            .pointerInput(key) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    pressed = true
                    respond(down.type.toInput())
                    waitForUpOrCancellation()
                    pressed = false
                }
            },
    ) {
        if (hint) Canvas(Modifier.matchParentSize()) {
            val grow = (if (model) 14.dp else 8.dp).toPx() * (1f + pulse * if (model) .8f else .4f)
            drawRoundRect(hue.lip, Offset(-grow, -grow), size.copy(width = size.width + 2 * grow, height = size.height + 2 * grow), CornerRadius(36.dp.toPx() + grow), style = Stroke((if (model) 7.dp else 5.dp).toPx()))
        }
        ChunkySurface(hue, if (solved) hue.tint else androidx.compose.ui.graphics.Color.White, Modifier.fillMaxSize(), lip = lip, maxLip = 9.dp, sink = pressSink(pressed, lip), corner = 32.dp) {
            Box(Modifier.fillMaxSize().padding(12.dp), contentAlignment = androidx.compose.ui.Alignment.Center, content = content)
            if (solved) CheckBadge(hue, Modifier.align(AbsoluteAlignment.TopRight).offset(x = 8.dp, y = (-8).dp))
        }
    }
}
