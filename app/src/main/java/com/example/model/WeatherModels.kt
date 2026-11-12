package com.example.model

data class CityLocation(
    val id: String,
    val name: String,
    val adm2: String = "",
    val adm1: String = "",
    val country: String = "中国",
    val lat: String = "",
    val lon: String = "",
    val isCurrentLocation: Boolean = false
) {
    val fullDisplayName: String
        get() {
            return when {
                adm1.isNotEmpty() && adm1 != name && adm2.isNotEmpty() && adm2 != name -> "$name, $adm2, $adm1"
                adm1.isNotEmpty() && adm1 != name -> "$name, $adm1"
                adm2.isNotEmpty() && adm2 != name -> "$name, $adm2"
                else -> name
            }
        }
}

data class WeatherNow(
    val obsTime: String = "",
    val temp: String = "22",
    val feelsLike: String = "23",
    val icon: String = "100",
    val text: String = "晴",
    val wind360: String = "0",
    val windDir: String = "东风",
    val windScale: String = "2",
    val windSpeed: String = "8",
    val humidity: String = "45",
    val precip: String = "0.0",
    val pressure: String = "1013",
    val vis: String = "20",
    val cloud: String = "10",
    val dew: String = "12"
)

data class HourlyForecast(
    val fxTime: String,
    val temp: String,
    val icon: String,
    val text: String,
    val windDir: String,
    val windScale: String = "2",
    val pop: String = "0",
    val humidity: String = "50"
)

data class DailyForecast(
    val fxDate: String,
    val tempMax: String,
    val tempMin: String,
    val iconDay: String,
    val textDay: String,
    val iconNight: String,
    val textNight: String,
    val windDirDay: String = "东风",
    val windScaleDay: String = "2",
    val humidity: String = "50",
    val precip: String = "0.0",
    val uvIndex: String = "5"
)

data class AirQuality(
    val aqi: String = "36",
    val level: String = "1",
    val category: String = "优",
    val primary: String = "NA",
    val pm10: String = "32",
    val pm2p5: String = "18",
    val no2: String = "22",
    val so2: String = "5",
    val co: String = "0.5",
    val o3: String = "48"
)

data class WeatherIndex(
    val type: String,
    val name: String,
    val level: String,
    val category: String,
    val text: String
)

data class WeatherWarning(
    val id: String = "",
    val title: String = "",
    val typeName: String = "",
    val level: String = "",
    val pubTime: String = "",
    val text: String = ""
)

data class FullWeatherData(
    val location: CityLocation,
    val now: WeatherNow,
    val hourly: List<HourlyForecast> = emptyList(),
    val daily: List<DailyForecast> = emptyList(),
    val air: AirQuality? = null,
    val indices: List<WeatherIndex> = emptyList(),
    val warnings: List<WeatherWarning> = emptyList(),
    val updateTimeFormatted: String = "",
    val isDemoData: Boolean = false,
    val isFromCache: Boolean = false
)

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val data: FullWeatherData) : WeatherUiState()
    data class Error(val message: String, val canRetry: Boolean = true) : WeatherUiState()
}
