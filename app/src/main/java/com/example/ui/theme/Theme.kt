package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.model.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = SkyBluePrimaryDark,
    onPrimary = SkyBlueOnPrimaryDark,
    primaryContainer = SkyBluePrimaryContainerDark,
    onPrimaryContainer = SkyBlueOnPrimaryContainerDark,
    secondary = AtmosphericSecondaryDark,
    onSecondary = AtmosphericOnSecondaryDark,
    secondaryContainer = AtmosphericSecondaryContainerDark,
    onSecondaryContainer = AtmosphericOnSecondaryContainerDark,
    tertiary = SunAmberTertiaryDark,
    onTertiary = SunAmberOnTertiaryDark,
    tertiaryContainer = SunAmberTertiaryContainerDark,
    onTertiaryContainer = SunAmberOnTertiaryContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = SkyBluePrimary,
    onPrimary = SkyBlueOnPrimary,
    primaryContainer = SkyBluePrimaryContainer,
    onPrimaryContainer = SkyBlueOnPrimaryContainer,
    secondary = AtmosphericSecondary,
    onSecondary = AtmosphericOnSecondary,
    secondaryContainer = AtmosphericSecondaryContainer,
    onSecondaryContainer = AtmosphericOnSecondaryContainer,
    tertiary = SunAmberTertiary,
    onTertiary = SunAmberOnTertiary,
    tertiaryContainer = SunAmberTertiaryContainer,
    onTertiaryContainer = SunAmberOnTertiaryContainer
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
