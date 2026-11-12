package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CityLookupResponse(
    @Json(name = "code") val code: String = "",
    @Json(name = "location") val location: List<LocationDto>? = null
)

@JsonClass(generateAdapter = true)
data class LocationDto(
    @Json(name = "name") val name: String = "",
    @Json(name = "id") val id: String = "",
    @Json(name = "lat") val lat: String = "",
    @Json(name = "lon") val lon: String = "",
    @Json(name = "adm2") val adm2: String = "",
    @Json(name = "adm1") val adm1: String = "",
    @Json(name = "country") val country: String = "中国",
    @Json(name = "tz") val tz: String = "",
    @Json(name = "type") val type: String = ""
)

@JsonClass(generateAdapter = true)
data class WeatherNowResponse(
    @Json(name = "code") val code: String = "",
    @Json(name = "updateTime") val updateTime: String? = null,
    @Json(name = "now") val now: WeatherNowDto? = null
)

@JsonClass(generateAdapter = true)
data class WeatherNowDto(
    @Json(name = "obsTime") val obsTime: String? = null,
    @Json(name = "temp") val temp: String = "0",
    @Json(name = "feelsLike") val feelsLike: String = "0",
    @Json(name = "icon") val icon: String = "100",
    @Json(name = "text") val text: String = "晴",
    @Json(name = "wind360") val wind360: String = "0",
    @Json(name = "windDir") val windDir: String = "北风",
    @Json(name = "windScale") val windScale: String = "1",
    @Json(name = "windSpeed") val windSpeed: String = "5",
    @Json(name = "humidity") val humidity: String = "50",
    @Json(name = "precip") val precip: String = "0.0",
    @Json(name = "pressure") val pressure: String = "1013",
    @Json(name = "vis") val vis: String = "10",
    @Json(name = "cloud") val cloud: String = "0",
    @Json(name = "dew") val dew: String = "0"
)

@JsonClass(generateAdapter = true)
data class WeatherHourlyResponse(
    @Json(name = "code") val code: String = "",
    @Json(name = "updateTime") val updateTime: String? = null,
    @Json(name = "hourly") val hourly: List<HourlyDto>? = null
)

@JsonClass(generateAdapter = true)
data class HourlyDto(
    @Json(name = "fxTime") val fxTime: String = "",
    @Json(name = "temp") val temp: String = "0",
    @Json(name = "icon") val icon: String = "100",
    @Json(name = "text") val text: String = "晴",
    @Json(name = "windDir") val windDir: String = "北风",
    @Json(name = "windScale") val windScale: String = "1",
    @Json(name = "pop") val pop: String = "0",
    @Json(name = "humidity") val humidity: String = "50"
)

@JsonClass(generateAdapter = true)
data class WeatherDailyResponse(
    @Json(name = "code") val code: String = "",
    @Json(name = "updateTime") val updateTime: String? = null,
    @Json(name = "daily") val daily: List<DailyDto>? = null
)

@JsonClass(generateAdapter = true)
data class DailyDto(
    @Json(name = "fxDate") val fxDate: String = "",
    @Json(name = "tempMax") val tempMax: String = "0",
    @Json(name = "tempMin") val tempMin: String = "0",
    @Json(name = "iconDay") val iconDay: String = "100",
    @Json(name = "textDay") val textDay: String = "晴",
    @Json(name = "iconNight") val iconNight: String = "150",
    @Json(name = "textNight") val textNight: String = "晴",
    @Json(name = "windDirDay") val windDirDay: String = "北风",
    @Json(name = "windScaleDay") val windScaleDay: String = "1",
    @Json(name = "humidity") val humidity: String = "50",
    @Json(name = "precip") val precip: String = "0.0",
    @Json(name = "uvIndex") val uvIndex: String = "3"
)

@JsonClass(generateAdapter = true)
data class AirNowResponse(
    @Json(name = "code") val code: String = "",
    @Json(name = "now") val now: AirDto? = null
)

@JsonClass(generateAdapter = true)
data class AirDto(
    @Json(name = "pubTime") val pubTime: String? = null,
    @Json(name = "aqi") val aqi: String = "40",
    @Json(name = "level") val level: String = "1",
    @Json(name = "category") val category: String = "优",
    @Json(name = "pm10") val pm10: String = "35",
    @Json(name = "pm2p5") val pm2p5: String = "20",
    @Json(name = "no2") val no2: String = "25",
    @Json(name = "so2") val so2: String = "6",
    @Json(name = "co") val co: String = "0.5",
    @Json(name = "o3") val o3: String = "50"
)

@JsonClass(generateAdapter = true)
data class IndicesResponse(
    @Json(name = "code") val code: String = "",
    @Json(name = "daily") val daily: List<IndexDto>? = null
)

@JsonClass(generateAdapter = true)
data class IndexDto(
    @Json(name = "date") val date: String = "",
    @Json(name = "type") val type: String = "1",
    @Json(name = "name") val name: String = "",
    @Json(name = "level") val level: String = "1",
    @Json(name = "category") val category: String = "",
    @Json(name = "text") val text: String = ""
)

@JsonClass(generateAdapter = true)
data class WarningResponse(
    @Json(name = "code") val code: String = "",
    @Json(name = "warning") val warning: List<WarningDto>? = null
)

@JsonClass(generateAdapter = true)
data class WarningDto(
    @Json(name = "id") val id: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "typeName") val typeName: String = "",
    @Json(name = "level") val level: String = "",
    @Json(name = "pubTime") val pubTime: String = "",
    @Json(name = "text") val text: String = ""
)
