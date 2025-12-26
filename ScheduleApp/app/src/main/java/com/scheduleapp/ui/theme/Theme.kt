package com.scheduleapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.OnPrimary,
    primaryContainer = AppColors.Primary.copy(alpha = 0.1f),
    onPrimaryContainer = AppColors.Primary,
    secondary = AppColors.TextSecondary,
    onSecondary = AppColors.OnPrimary,
    tertiary = AppColors.TextTertiary,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.CardBackground,
    onSurfaceVariant = AppColors.TextSecondary,
    outline = AppColors.Border,
    outlineVariant = AppColors.Divider,
    error = AppColors.Error,
    onError = AppColors.OnPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColorsDark.Primary,
    onPrimary = AppColorsDark.OnPrimary,
    primaryContainer = AppColorsDark.Primary.copy(alpha = 0.2f),
    onPrimaryContainer = AppColorsDark.Primary,
    secondary = AppColorsDark.TextSecondary,
    onSecondary = AppColorsDark.OnPrimary,
    tertiary = AppColorsDark.TextTertiary,
    background = AppColorsDark.Background,
    onBackground = AppColorsDark.TextPrimary,
    surface = AppColorsDark.Surface,
    onSurface = AppColorsDark.TextPrimary,
    surfaceVariant = AppColorsDark.CardBackground,
    onSurfaceVariant = AppColorsDark.TextSecondary,
    outline = AppColorsDark.Border,
    outlineVariant = AppColorsDark.Divider,
    error = AppColors.Error,
    onError = AppColors.OnPrimary
)

@Composable
fun ScheduleAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (darkTheme) {
                AppColorsDark.Background.toArgb()
            } else {
                AppColors.Background.toArgb()
            }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
