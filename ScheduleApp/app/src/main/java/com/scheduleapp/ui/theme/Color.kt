package com.scheduleapp.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * K-App Style Color Palette
 * Modern, minimal Korean app design system
 */
object AppColors {
    // Primary - Toss Blue style
    val Primary = Color(0xFF3182F6)
    val PrimaryVariant = Color(0xFF1B64DA)
    val OnPrimary = Color(0xFFFFFFFF)
    
    // Background
    val Background = Color(0xFFFFFFFF)
    val Surface = Color(0xFFF2F4F6)
    val CardBackground = Color(0xFFFFFFFF)
    
    // Text Colors (avoiding pure black for softer look)
    val TextPrimary = Color(0xFF191F28)
    val TextSecondary = Color(0xFF4E5968)
    val TextTertiary = Color(0xFF8E8E93)
    
    // Status Colors (sophisticated, not pure/bright)
    val Error = Color(0xFFF04452)
    val Success = Color(0xFF00D082)
    val Warning = Color(0xFFFF9500)
    
    // Priority Colors
    val PriorityHigh = Color(0xFFF04452)
    val PriorityMedium = Color(0xFFFF9500)
    val PriorityLow = Color(0xFF00D082)
    
    // Border & Divider
    val Border = Color(0xFFF2F4F6)
    val Divider = Color(0xFFE5E8EB)
    
    // Icon Colors
    val IconDefault = Color(0xFF4E5968)
    val IconActive = Color(0xFF3182F6)
    
    // Navigation Bar
    val NavBarBackground = Color(0xFFFFFFFF)
    val NavBarInactive = Color(0xFF8E8E93)
    val NavBarActive = Color(0xFF3182F6)
    
    // Calendar specific
    val CalendarToday = Color(0xFF3182F6)
    val CalendarSelected = Color(0xFF3182F6)
    val CalendarWeekend = Color(0xFFF04452)
    
    // Overlay
    val Scrim = Color(0x80000000)
}

// Dark theme colors (optional, for future expansion)
object AppColorsDark {
    val Primary = Color(0xFF3182F6)
    val PrimaryVariant = Color(0xFF5BA0F6)
    val OnPrimary = Color(0xFFFFFFFF)
    
    val Background = Color(0xFF121212)
    val Surface = Color(0xFF1E1E1E)
    val CardBackground = Color(0xFF2C2C2C)
    
    val TextPrimary = Color(0xFFE5E5EA)
    val TextSecondary = Color(0xFFAEAEB2)
    val TextTertiary = Color(0xFF6E6E73)
    
    val Border = Color(0xFF3A3A3C)
    val Divider = Color(0xFF3A3A3C)
}
