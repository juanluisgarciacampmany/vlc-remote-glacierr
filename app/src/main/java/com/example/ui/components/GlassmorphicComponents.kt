@file:Suppress("DEPRECATION")
package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Elegant Modern Translucent Colors reflecting Glassmorphism
object GlassTheme {
    val GlassBackground = Color(0xFF1E1E2E).copy(alpha = 0.45f)
    val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.12f)
    val GlowCyan = Color(0xFF00E5FF)
    val GlowViolet = Color(0xFFBD00FF)
    val GlowCrimson = Color(0xFFFF2A6D)
    
    val DarkObsidian = Color(0xFF0D0E1A)
    val SemiWhite = Color(0xFFE2E8F0)
    val MutedGray = Color(0xFF94A3B8)
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(20.dp),
    borderBrush: Brush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.20f),
            Color.White.copy(alpha = 0.03f)
        )
    ),
    backgroundBrush: Brush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.08f),
            Color.White.copy(alpha = 0.02f)
        )
    ),
    testTag: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    val tagModifier = if (testTag != null) {
        Modifier.testTag(testTag)
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .then(tagModifier)
            .clip(shape)
            .background(backgroundBrush)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .then(clickableModifier)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = GlassTheme.GlowCyan,
    enabled: Boolean = true,
    testTag: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val opacity = if (enabled) 0.15f else 0.05f
    val borderOpacity = if (enabled) 0.4f else 0.15f

    val tagModifier = if (testTag != null) {
        Modifier.testTag(testTag)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .then(tagModifier)
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        accentColor.copy(alpha = opacity),
                        Color.White.copy(alpha = 0.03f)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        accentColor.copy(alpha = borderOpacity),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = shape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    testTag: String? = null
) {
    val shape = RoundedCornerShape(14.dp)
    
    val tagModifier = if (testTag != null) {
        Modifier.testTag(testTag)
    } else {
        Modifier
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            color = GlassTheme.SemiWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .then(tagModifier)
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(shape)
                .background(Color.White.copy(alpha = 0.05f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.12f),
                    shape = shape
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(10.dp))
                }
                
                // Overlay text or basic original TextField to fit Glass aesthetics without XML styling
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(
                            text = placeholder,
                            color = GlassTheme.MutedGray.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    singleLine = true,
                    keyboardOptions = keyboardOptions,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
