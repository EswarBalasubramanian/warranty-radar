package org.example.project.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.theme.AppTheme

fun greetingFor(hour: Int, name: String): String = when (hour) {
    in 0..4 -> "Up late, $name?"
    in 5..11 -> "Good morning, $name"
    in 12..16 -> "Good afternoon, $name"
    else -> "Good evening, $name"
}

fun coverageSubtitle(totalItems: Int, attentionItems: Int): String = when {
    totalItems == 0 -> "Let's save your first receipt"
    attentionItems == 0 -> "Everything's covered. Sleep easy."
    attentionItems == 1 -> "One thing might be worth a look"
    else -> "$attentionItems things might be worth a look"
}

fun endsPhrase(days: Int): String = when {
    days <= 0 -> "ends today"
    days == 1 -> "ends tomorrow"
    days <= 13 -> "ends in $days days"
    else -> "ends in ${(days + 3) / 7} weeks"
}

fun heroStatusLine(days: Int?): String = when {
    days == null -> "Covered and safe"
    days >= 60 -> "Still cozy — ${friendlyTimeLeft(days)}"
    days >= 14 -> "Getting close — ${friendlyTimeLeft(days)}"
    else -> "Wrapping up — ${friendlyTimeLeft(days)}"
}

fun friendlyTimeLeft(days: Int): String = when {
    days <= 0 -> "Ends today"
    days == 1 -> "1 day left"
    days <= 13 -> "$days days left"
    days <= 55 -> "${(days + 3) / 7} weeks left"
    days <= 330 -> "${(days + 15) / 30} months left"
    days <= 550 -> "About a year left"
    else -> "${(days + 60) / 365} years of cover"
}

fun urgencyFraction(days: Int): Float = (days / 90f).coerceIn(0.06f, 1f)

fun Modifier.pressBounce(pressedScale: Float = 0.96f): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                pressed = true
                waitForUpOrCancellation()
                pressed = false
            }
        }
}

@Composable
fun ReceiptPerforation(modifier: Modifier = Modifier, color: Color = AppTheme.colors.divider) {
    Box(modifier = modifier.fillMaxWidth().height(1.dp).background(color))
}

@Composable
fun WarrantyRing(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = color.copy(alpha = 0.18f),
    strokeWidth: Dp = 3.dp
) {
    val animated = remember { Animatable(0f) }
    LaunchedEffect(fraction) {
        animated.animateTo(
            fraction.coerceIn(0f, 1f),
            spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
        )
    }
    Canvas(modifier = modifier) {
        val strokePx = strokeWidth.toPx()
        val inset = strokePx / 2
        val arcSize = Size(size.width - strokePx, size.height - strokePx)
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(strokePx, cap = StrokeCap.Round)
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = animated.value * 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(strokePx, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun SavedCelebration(visible: Boolean, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn() + scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            initialScale = 0.5f
        ),
        exit = fadeOut() + scaleOut(targetScale = 0.8f)
    ) {
        Surface(shape = RoundedCornerShape(50), color = colors.success) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text("Saved — you're covered", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
