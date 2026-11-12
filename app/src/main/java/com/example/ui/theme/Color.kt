package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Sky & Atmospheric MD3 Tones
val SkyBluePrimary = Color(0xFF006494)
val SkyBlueOnPrimary = Color(0xFFFFFFFF)
val SkyBluePrimaryContainer = Color(0xFFCBE6FF)
val SkyBlueOnPrimaryContainer = Color(0xFF001E30)

val AtmosphericSecondary = Color(0xFF50606E)
val AtmosphericOnSecondary = Color(0xFFFFFFFF)
val AtmosphericSecondaryContainer = Color(0xFFD3E5F5)
val AtmosphericOnSecondaryContainer = Color(0xFF0C1D29)

val SunAmberTertiary = Color(0xFF7E5700)
val SunAmberOnTertiary = Color(0xFFFFFFFF)
val SunAmberTertiaryContainer = Color(0xFFFFDEA8)
val SunAmberOnTertiaryContainer = Color(0xFF271900)

// Dark Theme Variants
val SkyBluePrimaryDark = Color(0xFF8FCDFF)
val SkyBlueOnPrimaryDark = Color(0xFF00344F)
val SkyBluePrimaryContainerDark = Color(0xFF004B71)
val SkyBlueOnPrimaryContainerDark = Color(0xFFCBE6FF)

val AtmosphericSecondaryDark = Color(0xFFB7C9D9)
val AtmosphericOnSecondaryDark = Color(0xFF22323F)
val AtmosphericSecondaryContainerDark = Color(0xFF384956)
val AtmosphericOnSecondaryContainerDark = Color(0xFFD3E5F5)

val SunAmberTertiaryDark = Color(0xFFFBBB48)
val SunAmberOnTertiaryDark = Color(0xFF432C00)
val SunAmberTertiaryContainerDark = Color(0xFF604100)
val SunAmberOnTertiaryContainerDark = Color(0xFFFFDEA8)

// Semantic AQI Colors
val AqiGood = Color(0xFF4CAF50)
val AqiModerate = Color(0xFFFFB300)
val AqiUnhealthySensitive = Color(0xFFFF9800)
val AqiUnhealthy = Color(0xFFF44336)
val AqiVeryUnhealthy = Color(0xFF9C27B0)
val AqiHazardous = Color(0xFF880E4F)

// Weather Gradients for dynamic background ambience
val SunnyGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF2196F3),
        Color(0xFF64B5F6),
        Color(0xFFE3F2FD)
    )
)

val NightGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0D1B2A),
        Color(0xFF1B263B),
        Color(0xFF415A77)
    )
)

val RainGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF37474F),
        Color(0xFF546E7A),
        Color(0xFFCFD8DC)
    )
)

val CloudGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF455A64),
        Color(0xFF78909C),
        Color(0xFFECEFF1)
    )
)
