package com.cutener.raising.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cutener.raising.ui.theme.NeoBrutalistColors
import com.cutener.raising.ui.theme.NeoBrutalistTheme

/**
 * Neo-Brutalist Button
 * Features: Bold black border, hard shadow, vivid background color
 */
@Composable
fun NeoBrutalistButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeoBrutalistColors.VividYellow,
    textColor: Color = NeoBrutalistColors.Black,
    borderColor: Color = NeoBrutalistColors.Black,
    shadowColor: Color = NeoBrutalistColors.Black,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val shadowOffset = if (isPressed) 2.dp else 4.dp
    val buttonOffset = if (isPressed) 2.dp else 0.dp
    
    val actualBgColor = if (enabled) backgroundColor else NeoBrutalistColors.LightGray
    val actualTextColor = if (enabled) textColor else NeoBrutalistColors.DarkGray

    Box(
        modifier = modifier
            .padding(end = 4.dp, bottom = 4.dp)
    ) {
        // Shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(shadowColor, RoundedCornerShape(4.dp))
        )
        
        // Button layer
        Box(
            modifier = Modifier
                .offset(x = buttonOffset, y = buttonOffset)
                .clip(RoundedCornerShape(4.dp))
                .background(actualBgColor)
                .border(
                    width = 3.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.invoke()
                if (icon != null) Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    color = actualTextColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

/**
 * Neo-Brutalist Action Button (Square, for actions like Train, Feed, Rest)
 */
@Composable
fun NeoBrutalistActionButton(
    text: String,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeoBrutalistColors.MintGreen,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val shadowOffset = if (isPressed) 2.dp else 4.dp
    val buttonOffset = if (isPressed) 2.dp else 0.dp
    
    val actualBgColor = if (enabled) backgroundColor else NeoBrutalistColors.LightGray

    Box(
        modifier = modifier
            .size(100.dp)
            .padding(end = 4.dp, bottom = 4.dp)
    ) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(NeoBrutalistColors.Black, RoundedCornerShape(8.dp))
        )
        
        // Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = buttonOffset, y = buttonOffset)
                .clip(RoundedCornerShape(8.dp))
                .background(actualBgColor)
                .border(3.dp, NeoBrutalistColors.Black, RoundedCornerShape(8.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = text,
                    color = NeoBrutalistColors.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Neo-Brutalist Card
 */
@Composable
fun NeoBrutalistCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeoBrutalistColors.White,
    borderColor: Color = NeoBrutalistColors.Black,
    shadowColor: Color = NeoBrutalistColors.Black,
    shadowOffset: Dp = 6.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .padding(end = shadowOffset, bottom = shadowOffset)
    ) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(shadowColor, RoundedCornerShape(8.dp))
        )
        
        // Card
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .border(3.dp, borderColor, RoundedCornerShape(8.dp))
                .padding(16.dp),
            content = content
        )
    }
}

/**
 * Neo-Brutalist Progress Bar
 */
@Composable
fun NeoBrutalistProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeoBrutalistColors.White,
    progressColor: Color = NeoBrutalistColors.HotPink,
    borderColor: Color = NeoBrutalistColors.Black,
    label: String? = null,
    showPercentage: Boolean = false
) {
    Column(modifier = modifier) {
        if (label != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeoBrutalistColors.Black
                )
                if (showPercentage) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NeoBrutalistColors.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(backgroundColor)
                .border(3.dp, borderColor, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(progressColor)
            )
        }
    }
}

/**
 * Neo-Brutalist Stat Bar with icon/emoji
 */
@Composable
fun NeoBrutalistStatBar(
    label: String,
    emoji: String,
    current: Int,
    max: Int,
    modifier: Modifier = Modifier,
    progressColor: Color = NeoBrutalistColors.HotPink
) {
    val progress = if (max > 0) current.toFloat() / max.toFloat() else 0f
    
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeoBrutalistColors.Black
                )
            }
            Text(
                text = "$current / $max",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = NeoBrutalistColors.Black
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(NeoBrutalistColors.White)
                .border(3.dp, NeoBrutalistColors.Black, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(progressColor)
            )
        }
    }
}

/**
 * Neo-Brutalist Text Field
 */
@Composable
fun NeoBrutalistTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    backgroundColor: Color = NeoBrutalistColors.White,
    borderColor: Color = NeoBrutalistColors.Black
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = NeoBrutalistColors.Black
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        
        Box(
            modifier = Modifier
                .padding(end = 4.dp, bottom = 4.dp)
        ) {
            // Shadow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 4.dp, y = 4.dp)
                    .background(NeoBrutalistColors.Black, RoundedCornerShape(4.dp))
            )
            
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(backgroundColor)
                    .border(3.dp, borderColor, RoundedCornerShape(4.dp))
                    .padding(16.dp),
                textStyle = TextStyle(
                    color = NeoBrutalistColors.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(NeoBrutalistColors.Black),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = NeoBrutalistColors.DarkGray.copy(alpha = 0.5f),
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

/**
 * Neo-Brutalist Dropdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeoBrutalistDropdown(
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    backgroundColor: Color = NeoBrutalistColors.White
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = NeoBrutalistColors.Black
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        
        Box(
            modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
        ) {
            // Shadow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 4.dp, y = 4.dp)
                    .background(NeoBrutalistColors.Black, RoundedCornerShape(4.dp))
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                Box(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(backgroundColor)
                        .border(3.dp, NeoBrutalistColors.Black, RoundedCornerShape(4.dp))
                        .clickable { expanded = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedValue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NeoBrutalistColors.Black
                        )
                        Text(
                            text = if (expanded) "▲" else "▼",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(NeoBrutalistColors.White)
                        .border(2.dp, NeoBrutalistColors.Black)
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (option == selectedValue) NeoBrutalistColors.VividYellow.copy(alpha = 0.3f)
                                    else Color.Transparent
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Neo-Brutalist Title/Header
 */
@Composable
fun NeoBrutalistTitle(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeoBrutalistColors.VividYellow
) {
    Box(
        modifier = modifier
            .padding(end = 4.dp, bottom = 4.dp)
    ) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(NeoBrutalistColors.Black, RoundedCornerShape(4.dp))
        )
        
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(backgroundColor)
                .border(3.dp, NeoBrutalistColors.Black, RoundedCornerShape(4.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                letterSpacing = 2.sp,
                color = NeoBrutalistColors.Black
            )
        }
    }
}

/**
 * Neo-Brutalist Badge
 */
@Composable
fun NeoBrutalistBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeoBrutalistColors.HotPink,
    textColor: Color = NeoBrutalistColors.White
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(2.dp, NeoBrutalistColors.Black, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            color = textColor,
            letterSpacing = 1.sp
        )
    }
}

/**
 * Neo-Brutalist Divider
 */
@Composable
fun NeoBrutalistDivider(
    modifier: Modifier = Modifier,
    color: Color = NeoBrutalistColors.Black,
    thickness: Dp = 3.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}
