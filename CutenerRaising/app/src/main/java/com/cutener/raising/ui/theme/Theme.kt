package com.cutener.raising.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NeoBrutalistLightColorScheme = lightColorScheme(
    primary = NeoBrutalistColors.HotPink,
    onPrimary = NeoBrutalistColors.Black,
    primaryContainer = NeoBrutalistColors.VividYellow,
    onPrimaryContainer = NeoBrutalistColors.Black,
    
    secondary = NeoBrutalistColors.ElectricBlue,
    onSecondary = NeoBrutalistColors.Black,
    secondaryContainer = NeoBrutalistColors.MintGreen,
    onSecondaryContainer = NeoBrutalistColors.Black,
    
    tertiary = NeoBrutalistColors.LimeGreen,
    onTertiary = NeoBrutalistColors.Black,
    tertiaryContainer = NeoBrutalistColors.VividPurple,
    onTertiaryContainer = NeoBrutalistColors.White,
    
    background = NeoBrutalistColors.CreamYellow,
    onBackground = NeoBrutalistColors.Black,
    
    surface = NeoBrutalistColors.White,
    onSurface = NeoBrutalistColors.Black,
    surfaceVariant = NeoBrutalistColors.SoftPink,
    onSurfaceVariant = NeoBrutalistColors.Black,
    
    error = NeoBrutalistColors.Error,
    onError = NeoBrutalistColors.White,
    
    outline = NeoBrutalistColors.Black,
    outlineVariant = NeoBrutalistColors.DarkGray
)

private val NeoBrutalistDarkColorScheme = darkColorScheme(
    primary = NeoBrutalistColors.VividYellow,
    onPrimary = NeoBrutalistColors.Black,
    primaryContainer = NeoBrutalistColors.HotPink,
    onPrimaryContainer = NeoBrutalistColors.White,
    
    secondary = NeoBrutalistColors.MintGreen,
    onSecondary = NeoBrutalistColors.Black,
    secondaryContainer = NeoBrutalistColors.ElectricBlue,
    onSecondaryContainer = NeoBrutalistColors.Black,
    
    tertiary = NeoBrutalistColors.LimeGreen,
    onTertiary = NeoBrutalistColors.Black,
    tertiaryContainer = NeoBrutalistColors.VividPurple,
    onTertiaryContainer = NeoBrutalistColors.White,
    
    background = NeoBrutalistColors.DarkGray,
    onBackground = NeoBrutalistColors.White,
    
    surface = NeoBrutalistColors.Black,
    onSurface = NeoBrutalistColors.White,
    surfaceVariant = NeoBrutalistColors.DarkGray,
    onSurfaceVariant = NeoBrutalistColors.White,
    
    error = NeoBrutalistColors.Error,
    onError = NeoBrutalistColors.Black,
    
    outline = NeoBrutalistColors.White,
    outlineVariant = NeoBrutalistColors.LightGray
)

@Composable
fun CutenerRaisingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NeoBrutalistDarkColorScheme else NeoBrutalistLightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primaryContainer.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
