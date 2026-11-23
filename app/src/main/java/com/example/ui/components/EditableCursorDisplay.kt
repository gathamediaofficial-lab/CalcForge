package com.example.ui.components

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.awaitCancellation

/**
 * Editable display field designed for custom-keypad calculators and unit converters.
 * Suppresses the system soft keyboard via InterceptPlatformTextInput while preserving
 * native tap-to-position cursor, drag-handle cursor movement, and range selection.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditableCursorDisplay(
    text: String,
    selection: TextRange,
    onSelectionChange: (TextRange) -> Unit,
    textStyle: TextStyle,
    cursorColor: Color = MaterialTheme.colorScheme.primary,
    placeholder: String = "0",
    placeholderColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
    highlighter: ((String) -> AnnotatedString)? = null,
    modifier: Modifier = Modifier
) {
    val safeSelection = TextRange(
        start = selection.start.coerceIn(0, text.length),
        end = selection.end.coerceIn(0, text.length)
    )
    val fieldValue = TextFieldValue(
        text = text,
        selection = safeSelection
    )

    val visualTransformation = remember(highlighter) {
        if (highlighter == null) {
            VisualTransformation.None
        } else {
            VisualTransformation { annotated ->
                TransformedText(
                    text = highlighter(annotated.text),
                    offsetMapping = OffsetMapping.Identity
                )
            }
        }
    }

    InterceptPlatformTextInput(
        interceptor = { _, _ -> awaitCancellation() }
    ) {
        BasicTextField(
            value = fieldValue,
            onValueChange = { updated ->
                // Update selection/cursor when user taps or drags the cursor handle
                val clamped = TextRange(
                    start = updated.selection.start.coerceIn(0, text.length),
                    end = updated.selection.end.coerceIn(0, text.length)
                )
                if (clamped != safeSelection) {
                    onSelectionChange(clamped)
                }
            },
            modifier = modifier,
            singleLine = true,
            textStyle = textStyle,
            cursorBrush = SolidColor(cursorColor),
            visualTransformation = visualTransformation,
            decorationBox = { innerTextField ->
                if (text.isEmpty()) {
                    androidx.compose.foundation.layout.Box(
                        contentAlignment = androidx.compose.ui.Alignment.CenterEnd
                    ) {
                        androidx.compose.material3.Text(
                            text = placeholder,
                            style = textStyle,
                            color = placeholderColor,
                            maxLines = 1,
                            softWrap = false
                        )
                        innerTextField()
                    }
                } else {
                    innerTextField()
                }
            }
        )
    }
}
