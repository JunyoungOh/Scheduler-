package com.cutener.raising.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Neo-Brutalism Color Palette
 * 
 * Design Keywords: Neo-Brutalism, High Contrast, Bold Borders, Vivid Colors
 * Inspired by: Tamagotchi & Digimon Device aesthetics
 */
object NeoBrutalistColors {
    // Primary Vivid Colors
    val VividYellow = Color(0xFFFFE500)
    val HotPink = Color(0xFFFF2D78)
    val ElectricBlue = Color(0xFF00BFFF)
    val LimeGreen = Color(0xFFBFFF00)
    val MintGreen = Color(0xFF00D9C0)
    val BrightOrange = Color(0xFFFF6B35)
    val VividPurple = Color(0xFF9B5DE5)
    val CyberCyan = Color(0xFF00F5D4)
    
    // Background Colors (Slightly muted vivid)
    val CreamYellow = Color(0xFFFFF8DC)
    val SoftPink = Color(0xFFFFE4EC)
    val SoftBlue = Color(0xFFE3F2FD)
    val SoftMint = Color(0xFFE0FFF4)
    val SoftPurple = Color(0xFFF3E5F5)
    val SoftOrange = Color(0xFFFFF3E0)
    
    // Core UI Colors
    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)
    val DarkGray = Color(0xFF2D2D2D)
    val LightGray = Color(0xFFF5F5F5)
    
    // Semantic Colors
    val Success = Color(0xFF00C853)
    val Warning = Color(0xFFFFAB00)
    val Error = Color(0xFFFF1744)
    val Info = Color(0xFF00B0FF)
    
    // Character Class Colors (For pixel art)
    val WarriorBlue = Color(0xFF1E90FF)
    val WarriorSilver = Color(0xFFC0C0C0)
    val MagePurple = Color(0xFF9932CC)
    val MagePink = Color(0xFFFF69B4)
    val PaladinGold = Color(0xFFFFD700)
    val PaladinWhite = Color(0xFFFFFACD)
    val DarkKnightBlack = Color(0xFF2F2F2F)
    val DarkKnightRed = Color(0xFFDC143C)
    val RogueGreen = Color(0xFF228B22)
    val RogueBrown = Color(0xFF8B4513)
    val ArcherBrown = Color(0xFFA0522D)
    val ArcherGreen = Color(0xFF32CD32)
    
    // Skin & Face Colors
    val SkinLight = Color(0xFFFFDBAC)
    val SkinMedium = Color(0xFFE5B887)
    val EyeBlack = Color(0xFF000000)
    val EyeWhite = Color(0xFFFFFFFF)
    val Blush = Color(0xFFFFB6C1)
    
    // Shadow for Neo-Brutalism (Hard shadow offset)
    val ShadowBlack = Color(0xFF000000)
    val ShadowOffset = 4f
}

/**
 * Theme configuration for Neo-Brutalism style
 */
object NeoBrutalistTheme {
    const val BorderWidth = 4f
    const val ShadowOffset = 4f
    const val CornerRadius = 0f // Neo-Brutalism uses sharp corners or minimal rounding
    const val SmallCornerRadius = 4f
    const val CardElevation = 0f // We use hard shadows instead
}
