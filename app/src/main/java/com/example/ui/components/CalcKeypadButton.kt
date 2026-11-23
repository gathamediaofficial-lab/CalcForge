package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFontFamily

val LocalHapticsEnabled = compositionLocalOf { true }

@Composable
fun CalcKeypadButton(
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    shape: Shape = RoundedCornerShape(50),
    fontSizeSp: Float = 20f,
    fontWeight: FontWeight = FontWeight.SemiBold,
    minHeight: Dp = 48.dp,
    icon: ImageVector? = null,
    contentDescription: String? = null,
    testTag: String = "key_$label"
) {
    val haptic = LocalHapticFeedback.current
    val hapticsEnabled = LocalHapticsEnabled.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "keyScale"
    )

    Surface(
        onClick = {
            if (hapticsEnabled) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            onClick()
        },
        modifier = modifier
            .heightIn(min = minHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .testTag(testTag),
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = borderColor?.let { BorderStroke(1.dp, it) },
        interactionSource = interactionSource,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription ?: label,
                    tint = contentColor
                )
            } else {
                Text(
                    text = label,
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = fontWeight,
                        fontSize = fontSizeSp.sp,
                        fontFeatureSettings = "tnum"
                    ),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}
