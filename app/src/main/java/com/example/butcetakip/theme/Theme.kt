package com.example.butcetakip.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppTheme {
    SYSTEM, LIGHT, DARK, AMOLED
}

private val LightColorScheme = lightColorScheme(
    primary = PremiumDarkCyan,
    secondary = PremiumTurquoise,
    tertiary = PremiumEmerald,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = FintechRed
)

private val DarkColorScheme = darkColorScheme(
    primary = PremiumTurquoise,
    secondary = PremiumDarkCyan,
    tertiary = PremiumEmerald,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = FintechRed
)

private val AmoledColorScheme = darkColorScheme(
    primary = PremiumTurquoise,
    secondary = PremiumDarkCyan,
    tertiary = PremiumEmerald,
    background = AmoledBackground,
    surface = AmoledSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onBackground = AmoledOnBackground,
    onSurface = AmoledOnSurface,
    onSurfaceVariant = AmoledOnSurfaceVariant,
    outline = AmoledOutline,
    error = FintechRed
)

@Composable
fun ButceTakipTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.AMOLED -> AmoledColorScheme
        AppTheme.SYSTEM -> if (darkTheme) DarkColorScheme else LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = 
                (appTheme == AppTheme.LIGHT) || (appTheme == AppTheme.SYSTEM && !darkTheme)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
