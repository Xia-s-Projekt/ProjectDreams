package com.projectdreams.app.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.projectdreams.app.ui.theme.AbsoluteSmoothCornerShape

/**
 * Animates scale to [pressedScale] on press with bouncy spring physics.
 * Uses `graphicsLayer` so touch bounds are NOT shrunk (prevents gesture cancellation).
 * Implements a 180ms hold on release so fast taps always show the bounce.
 */
@Composable
fun Modifier.bouncyPress(
    pressedScale: Float = 0.90f,
    enabled: Boolean = true
): Modifier {
    if (!enabled) return this
    val scale = remember { Animatable(1f) }
    var pressed by remember { mutableStateOf(false) }
    val springSpec = LocalBouncySpring.current

    LaunchedEffect(pressed) {
        if (pressed) {
            scale.animateTo(pressedScale, springSpec)
        } else {
            delay(180)
            scale.animateTo(1f, springSpec)
        }
    }

    return this
        .pointerInput(enabled) {
            detectTapGestures(
                onPress = {
                    pressed = true
                    tryAwaitRelease()
                    pressed = false
                }
            )
        }
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
}

/** Button with deep bouncy scale-down on press. */
@Composable
fun BouncyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = AbsoluteSmoothCornerShape(16.dp, 60),
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }
    val springSpec = LocalBouncySpring.current

    LaunchedEffect(isPressed) {
        if (isPressed) {
            scale.animateTo(0.90f, springSpec)
        } else {
            delay(180)
            scale.animateTo(1f, springSpec)
        }
    }

    Button(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
        enabled = enabled,
        shape = shape,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

/** OutlinedButton with deep bouncy scale-down on press. */
@Composable
fun BouncyOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = AbsoluteSmoothCornerShape(14.dp, 60),
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }
    val springSpec = LocalBouncySpring.current

    LaunchedEffect(isPressed) {
        if (isPressed) {
            scale.animateTo(0.90f, springSpec)
        } else {
            delay(180)
            scale.animateTo(1f, springSpec)
        }
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
        enabled = enabled,
        shape = shape,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

/** TextButton with bouncy scale-down on press. */
@Composable
fun BouncyTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }
    val springSpec = LocalBouncySpring.current

    LaunchedEffect(isPressed) {
        if (isPressed) {
            scale.animateTo(0.92f, springSpec)
        } else {
            delay(180)
            scale.animateTo(1f, springSpec)
        }
    }

    TextButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
        enabled = enabled,
        interactionSource = interactionSource,
        content = content
    )
}

/** Card with bouncy scale-down on press/click. */
@Composable
fun BouncyCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = AbsoluteSmoothCornerShape(24.dp, 60),
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }
    val springSpec = LocalBouncySpring.current

    LaunchedEffect(isPressed) {
        if (isPressed) {
            scale.animateTo(0.95f, springSpec)
        } else {
            delay(180)
            scale.animateTo(1f, springSpec)
        }
    }

    Card(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
        shape = shape,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

/** Non-clickable Card with squircle shape. */
@Composable
fun SquircleCard(
    modifier: Modifier = Modifier,
    shape: Shape = AbsoluteSmoothCornerShape(24.dp, 60),
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        content = content
    )
}

/** Switch with bouncy scale-down on toggle. */
@Composable
fun BouncySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors()
) {
    val scale = remember { Animatable(1f) }
    val springSpec = LocalBouncySpring.current

    LaunchedEffect(checked) {
        scale.snapTo(0.85f)
        delay(80)
        scale.animateTo(1f, springSpec)
    }

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
        enabled = enabled,
        colors = colors
    )
}

/** IconButton with bouncy scale-down on press. */
@Composable
fun BouncyIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }
    val springSpec = LocalBouncySpring.current

    LaunchedEffect(isPressed) {
        if (isPressed) {
            scale.animateTo(0.85f, springSpec)
        } else {
            delay(180)
            scale.animateTo(1f, springSpec)
        }
    }

    IconButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
        enabled = enabled,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Animated sine-wave (squiggly) progress indicator.
 * The fill line wiggles as it progresses.
 */
@Composable
fun WavyProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    trackColor: androidx.compose.ui.graphics.Color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
    progressColor: androidx.compose.ui.graphics.Color = androidx.compose.material3.MaterialTheme.colorScheme.primary
) {
    val animatedPhase = remember { Animatable(0f) }
    val currentProgress = progress()

    LaunchedEffect(Unit) {
        while (true) {
            animatedPhase.animateTo(
                animatedPhase.value + 360f,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 2000,
                    easing = androidx.compose.animation.core.LinearEasing
                )
            )
        }
    }

    androidx.compose.foundation.Canvas(
        modifier = modifier
    ) {
        val width = size.width
        val height = size.height
        val midY = height / 2f
        val amplitude = height * 0.25f
        val wavelength = 40.dp.toPx()
        val fillWidth = width * currentProgress.coerceIn(0f, 1f)
        val phase = Math.toRadians(animatedPhase.value.toDouble()).toFloat()

        // Background track
        val trackPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, midY)
            var x = 0f
            while (x <= width) {
                val y = midY + amplitude * kotlin.math.sin((x / wavelength) * 2f * Math.PI.toFloat() + phase * 0.3f)
                lineTo(x, y)
                x += 2f
            }
        }
        drawPath(
            path = trackPath,
            color = trackColor.copy(alpha = 0.3f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = height * 0.6f)
        )

        // Fill wave
        if (fillWidth > 0f) {
            val fillPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, midY)
                var fx = 0f
                while (fx <= fillWidth) {
                    val y = midY + amplitude * kotlin.math.sin((fx / wavelength) * 2f * Math.PI.toFloat() + phase)
                    lineTo(fx, y)
                    fx += 2f
                }
            }
            drawPath(
                path = fillPath,
                color = progressColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = height * 0.6f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }
    }
}
