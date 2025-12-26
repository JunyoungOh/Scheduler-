package com.scheduleapp.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.scheduleapp.R

/**
 * Pretendard Font Family
 * Modern Korean typography following K-App design guidelines
 */
val PretendardFontFamily = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold)
)

/**
 * App Typography System
 * Following K-App design guidelines:
 * - Title: 18-22px (Bold/SemiBold)
 * - Body: 15-16px (Medium/Regular)
 * - Caption: 12-13px (Regular)
 * - Line Height: 1.5-1.6 for readability
 */
object AppTypography {
    
    // Large Title - Screen headers
    val LargeTitle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        color = AppColors.TextPrimary
    )
    
    // Title 1 - Section headers
    val Title1 = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = AppColors.TextPrimary
    )
    
    // Title 2 - Card titles
    val Title2 = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        color = AppColors.TextPrimary
    )
    
    // Title 3 - List item titles
    val Title3 = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = AppColors.TextPrimary
    )
    
    // Body 1 - Main body text
    val Body1 = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = AppColors.TextPrimary
    )
    
    // Body 2 - Secondary body text
    val Body2 = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = AppColors.TextSecondary
    )
    
    // Body Medium - Emphasized body text
    val BodyMedium = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = AppColors.TextPrimary
    )
    
    // Caption 1 - Metadata, timestamps
    val Caption1 = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = AppColors.TextTertiary
    )
    
    // Caption 2 - Small labels
    val Caption2 = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = AppColors.TextTertiary
    )
    
    // Button - Button text
    val Button = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = AppColors.OnPrimary
    )
    
    // Navigation - Bottom nav labels
    val Navigation = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
}
