package com.scheduleapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * App Shape System
 * Following K-App design guidelines:
 * - Border-radius: 16px or 20px for cards
 * - Button radius: 12px
 */
object AppShapes {
    // Small - Tags, chips
    val Small = RoundedCornerShape(8.dp)
    
    // Medium - Buttons, small cards
    val Medium = RoundedCornerShape(12.dp)
    
    // Large - Cards, dialogs
    val Large = RoundedCornerShape(16.dp)
    
    // Extra Large - Bottom sheets, large cards
    val ExtraLarge = RoundedCornerShape(20.dp)
    
    // Top Only - Bottom sheets
    val TopLarge = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    
    // Circle - FAB, Avatar
    val Circle = RoundedCornerShape(50)
}

/**
 * Spacing System
 * Based on 8dp grid system
 */
object AppSpacing {
    val XXSmall = 4.dp
    val XSmall = 8.dp
    val Small = 12.dp
    val Medium = 16.dp
    val Large = 24.dp
    val XLarge = 32.dp
    val XXLarge = 48.dp
    
    // Specific use cases
    val ScreenPadding = 20.dp
    val CardPadding = 16.dp
    val ListItemSpacing = 12.dp
    val SectionSpacing = 24.dp
}

/**
 * Elevation values for shadows
 */
object AppElevation {
    val None = 0.dp
    val Low = 2.dp
    val Medium = 4.dp
    val High = 8.dp
}

/**
 * Icon sizes
 */
object AppIconSize {
    val Small = 20.dp
    val Default = 24.dp
    val Large = 28.dp
    val XLarge = 32.dp
}
