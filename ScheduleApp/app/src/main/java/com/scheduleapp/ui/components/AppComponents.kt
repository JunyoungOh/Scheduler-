package com.scheduleapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.scheduleapp.data.model.Priority
import com.scheduleapp.ui.theme.*

/**
 * App-styled card container
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = AppShapes.Large,
        color = AppColors.CardBackground,
        shadowElevation = AppElevation.Low
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.CardPadding),
            content = content
        )
    }
}

/**
 * Primary button following K-App design
 */
@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        shape = AppShapes.Medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Primary,
            contentColor = AppColors.OnPrimary,
            disabledContainerColor = AppColors.Primary.copy(alpha = 0.5f),
            disabledContentColor = AppColors.OnPrimary.copy(alpha = 0.5f)
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AppIconSize.Default)
            )
            Spacer(modifier = Modifier.width(AppSpacing.XSmall))
        }
        Text(
            text = text,
            style = AppTypography.Button
        )
    }
}

/**
 * Secondary button (outlined)
 */
@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        shape = AppShapes.Medium,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AppColors.Primary
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(AppColors.Primary)
        )
    ) {
        Text(
            text = text,
            style = AppTypography.Button.copy(color = AppColors.Primary)
        )
    }
}

/**
 * Floating action button
 */
@Composable
fun AppFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Add
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = AppShapes.Circle,
        containerColor = AppColors.Primary,
        contentColor = AppColors.OnPrimary
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "추가",
            modifier = Modifier.size(AppIconSize.Default)
        )
    }
}

/**
 * Top app bar following K-App design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = AppTypography.Title1
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로",
                        tint = AppColors.TextPrimary
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppColors.Background,
            titleContentColor = AppColors.TextPrimary
        )
    )
}

/**
 * Custom checkbox with completion animation
 */
@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) AppColors.Primary else Color.Transparent,
        animationSpec = tween(200),
        label = "checkbox_bg"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (checked) AppColors.Primary else AppColors.Border,
        animationSpec = tween(200),
        label = "checkbox_border"
    )
    
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(2.dp, borderColor, CircleShape)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = AppColors.OnPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Priority indicator badge
 */
@Composable
fun PriorityBadge(
    priority: Priority,
    modifier: Modifier = Modifier
) {
    val color = when (priority) {
        Priority.HIGH -> AppColors.PriorityHigh
        Priority.MEDIUM -> AppColors.PriorityMedium
        Priority.LOW -> AppColors.PriorityLow
    }
    
    Surface(
        modifier = modifier,
        shape = AppShapes.Small,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = priority.displayName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = AppTypography.Caption2.copy(color = color)
        )
    }
}

/**
 * Empty state placeholder
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppSpacing.XLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = AppColors.TextTertiary
        )
        Spacer(modifier = Modifier.height(AppSpacing.Medium))
        Text(
            text = title,
            style = AppTypography.Title3.copy(color = AppColors.TextSecondary)
        )
        Spacer(modifier = Modifier.height(AppSpacing.XSmall))
        Text(
            text = description,
            style = AppTypography.Body2.copy(color = AppColors.TextTertiary)
        )
    }
}

/**
 * Section header
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.Small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppTypography.Title3
        )
        action?.invoke()
    }
}

/**
 * Filter chip
 */
@Composable
fun AppFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(AppShapes.Circle)
            .clickable(onClick = onClick),
        shape = AppShapes.Circle,
        color = if (selected) AppColors.Primary else AppColors.Surface
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = AppTypography.BodyMedium.copy(
                color = if (selected) AppColors.OnPrimary else AppColors.TextSecondary
            )
        )
    }
}
