package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WeatherConditionIcon(
    iconCode: String,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    tint: Color? = null
) {
    val codeNum = iconCode.toIntOrNull() ?: 100

    val (icon, defaultColor) = when (codeNum) {
        100 -> Icons.Filled.WbSunny to Color(0xFFFFB300) // Sunny
        in 101..103 -> Icons.Filled.WbCloudy to Color(0xFF64B5F6) // Cloudy
        104 -> Icons.Filled.Cloud to Color(0xFF90A4AE) // Overcast
        in 150..153 -> Icons.Filled.Nightlight to Color(0xFF9FA8DA) // Night clear/cloudy
        in 300..304 -> Icons.Filled.Thunderstorm to Color(0xFF7E57C2) // Thunderstorm
        in 305..313 -> Icons.Filled.WaterDrop to Color(0xFF29B6F6) // Rain
        in 314..399 -> Icons.Filled.Grain to Color(0xFF0288D1) // Heavy rain
        in 400..499 -> Icons.Filled.AcUnit to Color(0xFF80DEEA) // Snow
        in 500..599 -> Icons.Filled.Air to Color(0xFFB0BEC5) // Fog / Haze
        else -> Icons.Outlined.WbSunny to Color(0xFFFFB300)
    }

    Icon(
        imageVector = icon,
        contentDescription = "天气图标 $iconCode",
        tint = tint ?: defaultColor,
        modifier = modifier.size(size)
    )
}
