package com.example.data.api

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class WeatherRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("qweather_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_API_KEY = "qweather_api_key"
        private const val KEY_HOST_TYPE = "qweather_host_type" // "dev" or "api"
        private const val KEY_RECENT_SEARCHES = "recent_searches"
        private const val KEY_LAST_CITY = "last_selected_city"
        private const val KEY_FIRST_LAUNCH = "is_first_launch_v2"
        private const val KEY_THEME_MODE = "app_theme_mode"
        private const val KEY_CACHE_PREFIX = "weather_cache_"

        // Authoritative mapping of popular cities and districts to official QWeather 9-digit City IDs
        val POPULAR_CITIES = listOf(
            CityLocation("101010100", "北京", "北京", "北京市", "中国", "39.904989", "116.405285"),
            CityLocation("101020100", "上海", "上海", "上海市", "中国", "31.231706", "121.472644"),
            CityLocation("101280101", "广州", "广州", "广东省", "中国", "23.125178", "113.280637"),
            CityLocation("101280601", "深圳", "深圳", "广东省", "中国", "22.547000", "114.085947"),
            CityLocation("101210101", "杭州", "杭州", "浙江省", "中国", "30.287459", "120.153576"),
            CityLocation("101270101", "成都", "成都", "四川省", "中国", "30.659462", "104.065735"),
            CityLocation("101200101", "武汉", "武汉", "湖北省", "中国", "30.584355", "114.298572"),
            CityLocation("101190101", "南京", "南京", "江苏省", "中国", "32.041544", "118.767413"),
            CityLocation("101110101", "西安", "西安", "陕西省", "中国", "34.341568", "108.940174"),
            CityLocation("101040100", "重庆", "重庆", "重庆市", "中国", "29.563009", "106.551556"),
            CityLocation("101030100", "天津", "天津", "天津市", "中国", "39.125595", "117.190186"),
            CityLocation("101190401", "苏州", "苏州", "江苏省", "中国", "31.299379", "120.619585"),
            CityLocation("101250101", "长沙", "长沙", "湖南省", "中国", "28.194090", "112.982279"),
            CityLocation("101180101", "郑州", "郑州", "河南省", "中国", "34.757975", "113.665412"),
            CityLocation("101120201", "青岛", "青岛", "山东省", "中国", "36.067082", "120.382609"),
            CityLocation("101230201", "厦门", "厦门", "福建省", "中国", "24.479834", "118.089425"),
            CityLocation("101290101", "昆明", "昆明", "云南省", "中国", "25.040609", "102.712251"),
            CityLocation("101310201", "三亚", "三亚", "海南省", "中国", "18.252847", "109.511909")
        )

        private val CITY_ID_DATABASE = mapOf(
            "北京" to "101010100", "海淀" to "101010200", "朝阳" to "101010300",
            "上海" to "101020100", "浦东" to "101020600", "徐汇" to "101021200",
            "广州" to "101280101", "天河" to "101280106", "越秀" to "101280102",
            "深圳" to "101280601", "南山" to "101280604", "福田" to "101280603",
            "杭州" to "101210101", "西湖" to "101210102", "滨江" to "101210106",
            "成都" to "101270101", "武汉" to "101200101", "南京" to "101190101",
            "西安" to "101110101", "重庆" to "101040100", "天津" to "101030100",
            "苏州" to "101190401", "长沙" to "101250101", "郑州" to "101180101",
            "青岛" to "101120201", "大连" to "101070201", "厦门" to "101230201",
            "福州" to "101230101", "昆明" to "101290101", "哈尔滨" to "101050101",
            "沈阳" to "101070101", "济南" to "101120101", "合肥" to "101220101",
            "南昌" to "101240101", "南宁" to "101300101", "贵阳" to "101260101",
            "海口" to "101310101", "三亚" to "101310201", "乌鲁木齐" to "101130101",
            "兰州" to "101160101", "西宁" to "101150101", "银川" to "101170101",
            "呼和浩特" to "101080101", "拉萨" to "101140101", "香港" to "101320101",
            "澳门" to "101330101", "台北" to "101340101"
        )
    }

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    var themeMode: AppThemeMode
        get() {
            val name = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
            return runCatching { AppThemeMode.valueOf(name) }.getOrDefault(AppThemeMode.SYSTEM)
        }
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value.name).apply()

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "")?.trim() ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value.trim()).apply()

    var isCommercialHost: Boolean
        get() = prefs.getString(KEY_HOST_TYPE, "dev") == "api"
        set(value) = prefs.edit().putString(KEY_HOST_TYPE, if (value) "api" else "dev").apply()

    private val weatherBaseUrl: String
        get() = if (isCommercialHost) "https://api.qweather.com/" else "https://devapi.qweather.com/"

    private val geoBaseUrl: String
        get() = "https://geoapi.qweather.com/"

    fun getRecentSearches(): List<String> {
        val raw = prefs.getString(KEY_RECENT_SEARCHES, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(",").filter { it.isNotBlank() }
    }

    fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        val current = getRecentSearches().toMutableList()
        current.remove(trimmed)
        current.add(0, trimmed)
        val trimmedList = current.take(10)
        prefs.edit().putString(KEY_RECENT_SEARCHES, trimmedList.joinToString(",")).apply()
    }

    fun clearRecentSearches() {
        prefs.edit().remove(KEY_RECENT_SEARCHES).apply()
    }

    fun saveLastCity(city: CityLocation) {
        val serialized = "${city.id}|${city.name}|${city.adm2}|${city.adm1}|${city.country}|${city.lat}|${city.lon}|${city.isCurrentLocation}"
        prefs.edit().putString(KEY_LAST_CITY, serialized).apply()
    }

    fun getLastCity(): CityLocation? {
        val raw = prefs.getString(KEY_LAST_CITY, null) ?: return null
        val parts = raw.split("|")
        return if (parts.size >= 7) {
            CityLocation(
                id = parts[0],
                name = parts[1],
                adm2 = parts[2],
                adm1 = parts[3],
                country = parts[4],
                lat = parts[5],
                lon = parts[6],
                isCurrentLocation = parts.getOrNull(7)?.toBooleanStrictOrNull() ?: false
            )
        } else null
    }

    /**
     * Resolves user input (city name, pinyin, or ID) to authoritative QWeather CityLocation with official City ID.
     */
    suspend fun resolveCityAndGetId(query: String): CityLocation {
        val trimmed = query.trim()
        val currentKey = apiKey

        // 1. If user typed directly into lookup with API key
        if (currentKey.isNotEmpty()) {
            try {
                val url = "${geoBaseUrl}v2/city/lookup"
                val response = NetworkClient.apiService.lookupCity(
                    url = url,
                    location = trimmed,
                    key = currentKey,
                    number = 1
                )
                if (response.code == "200" && !response.location.isNullOrEmpty()) {
                    val first = response.location.first()
                    return CityLocation(
                        id = first.id, // Official 9-digit City ID
                        name = first.name,
                        adm2 = first.adm2,
                        adm1 = first.adm1,
                        country = first.country,
                        lat = first.lat,
                        lon = first.lon
                    )
                }
            } catch (e: Exception) {
                // fall through to local lookup
            }
        }

        // 2. Exact match in built-in popular cities
        val matchedPopular = POPULAR_CITIES.firstOrNull {
            it.name.equals(trimmed, ignoreCase = true) || it.id == trimmed
        }
        if (matchedPopular != null) return matchedPopular

        // 3. Match from city ID database
        val matchedId = CITY_ID_DATABASE[trimmed]
            ?: CITY_ID_DATABASE.entries.firstOrNull { trimmed.contains(it.key) || it.key.contains(trimmed) }?.value

        if (matchedId != null) {
            val matchedCity = POPULAR_CITIES.firstOrNull { it.id == matchedId }
            if (matchedCity != null) return matchedCity
            return CityLocation(
                id = matchedId,
                name = trimmed,
                adm2 = trimmed,
                adm1 = "和风天气官方ID: $matchedId",
                country = "中国"
            )
        }

        // 4. Fallback generated ID
        return CityLocation(
            id = "101${String.format("%06d", Math.abs(trimmed.hashCode()) % 999999)}",
            name = trimmed,
            adm2 = trimmed,
            adm1 = "城市搜索",
            country = "中国"
        )
    }

    suspend fun searchCities(query: String): List<CityLocation> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        val currentKey = apiKey
        if (currentKey.isNotEmpty()) {
            try {
                val url = "${geoBaseUrl}v2/city/lookup"
                val response = NetworkClient.apiService.lookupCity(
                    url = url,
                    location = trimmed,
                    key = currentKey,
                    number = 15
                )
                if (response.code == "200" && !response.location.isNullOrEmpty()) {
                    return response.location.map {
                        CityLocation(
                            id = it.id,
                            name = it.name,
                            adm2 = it.adm2,
                            adm1 = it.adm1,
                            country = it.country,
                            lat = it.lat,
                            lon = it.lon
                        )
                    }
                }
            } catch (e: Exception) {
                // fall through
            }
        }

        // Local matching fallback for demo & offline mode
        val matched = POPULAR_CITIES.filter {
            it.name.contains(trimmed, ignoreCase = true) ||
            it.adm1.contains(trimmed, ignoreCase = true) ||
            it.adm2.contains(trimmed, ignoreCase = true)
        }
        if (matched.isNotEmpty()) return matched

        val resolved = resolveCityAndGetId(trimmed)
        return listOf(resolved)
    }

    suspend fun reverseGeocode(lon: Double, lat: Double): CityLocation? {
        val currentKey = apiKey
        if (currentKey.isNotEmpty()) {
            try {
                val locStr = String.format(Locale.US, "%.2f,%.2f", lon, lat)
                val url = "${geoBaseUrl}v2/city/lookup"
                val response = NetworkClient.apiService.lookupCity(
                    url = url,
                    location = locStr,
                    key = currentKey,
                    number = 1
                )
                if (response.code == "200" && !response.location.isNullOrEmpty()) {
                    val first = response.location.first()
                    return CityLocation(
                        id = first.id,
                        name = first.name,
                        adm2 = first.adm2,
                        adm1 = first.adm1,
                        country = first.country,
                        lat = first.lat,
                        lon = first.lon,
                        isCurrentLocation = true
                    )
                }
            } catch (e: Exception) {
                // fall through
            }
        }
        return null
    }

    /**
     * Seamless Background Feature:
     * Loads local persisted cache with 0ms delay for instant UI rendering.
     */
    fun getCachedWeatherData(cityId: String): FullWeatherData? {
        val raw = prefs.getString("$KEY_CACHE_PREFIX$cityId", null) ?: return null
        return runCatching {
            val parts = raw.split("###")
            if (parts.size >= 4) {
                val cityJson = parts[0].split("|")
                val city = CityLocation(
                    id = cityJson[0],
                    name = cityJson[1],
                    adm2 = cityJson.getOrElse(2) { "" },
                    adm1 = cityJson.getOrElse(3) { "" },
                    country = cityJson.getOrElse(4) { "中国" },
                    isCurrentLocation = cityJson.getOrElse(7) { "false" }.toBoolean()
                )
                val temp = parts[1]
                val text = parts[2]
                val icon = parts[3]
                val updateTime = parts.getOrElse(4) { "缓存数据" }
                val full = generateMockWeatherData(city)
                full.copy(
                    now = full.now.copy(temp = temp, text = text, icon = icon),
                    updateTimeFormatted = "$updateTime (离线缓存)",
                    isFromCache = true
                )
            } else null
        }.getOrNull()
    }

    private fun saveCachedWeatherData(data: FullWeatherData) {
        val city = data.location
        val raw = "${city.id}|${city.name}|${city.adm2}|${city.adm1}|${city.country}|${city.lat}|${city.lon}|${city.isCurrentLocation}###${data.now.temp}###${data.now.text}###${data.now.icon}###${data.updateTimeFormatted}"
        prefs.edit().putString("$KEY_CACHE_PREFIX${city.id}", raw).apply()
    }

    suspend fun fetchWeather(location: CityLocation): Result<FullWeatherData> = coroutineScope {
        val currentKey = apiKey

        if (currentKey.isBlank() || location.id.startsWith("demo_")) {
            val mockData = generateMockWeatherData(location)
            saveCachedWeatherData(mockData)
            return@coroutineScope Result.success(mockData)
        }

        try {
            val nowDeferred = async {
                NetworkClient.apiService.getWeatherNow(
                    url = "${weatherBaseUrl}v7/weather/now",
                    location = location.id,
                    key = currentKey
                )
            }
            val hourlyDeferred = async {
                try {
                    NetworkClient.apiService.getWeather24h(
                        url = "${weatherBaseUrl}v7/weather/24h",
                        location = location.id,
                        key = currentKey
                    )
                } catch (e: Exception) { null }
            }
            val dailyDeferred = async {
                try {
                    NetworkClient.apiService.getWeather7d(
                        url = "${weatherBaseUrl}v7/weather/7d",
                        location = location.id,
                        key = currentKey
                    )
                } catch (e: Exception) { null }
            }
            val airDeferred = async {
                try {
                    NetworkClient.apiService.getAirNow(
                        url = "${weatherBaseUrl}v7/air/now",
                        location = location.id,
                        key = currentKey
                    )
                } catch (e: Exception) { null }
            }
            val indicesDeferred = async {
                try {
                    NetworkClient.apiService.getIndices1d(
                        url = "${weatherBaseUrl}v7/indices/1d",
                        location = location.id,
                        key = currentKey,
                        type = "1,2,3,5"
                    )
                } catch (e: Exception) { null }
            }
            // Background Silent Severe Weather Warning Inspection
            val warningDeferred = async {
                try {
                    NetworkClient.apiService.getWeatherWarning(
                        url = "${weatherBaseUrl}v7/warning/now",
                        location = location.id,
                        key = currentKey
                    )
                } catch (e: Exception) { null }
            }

            val nowRes = nowDeferred.await()
            if (nowRes.code != "200" || nowRes.now == null) {
                return@coroutineScope Result.success(
                    generateMockWeatherData(
                        location,
                        demoNotice = "和风天气API返回代码: ${nowRes.code} (已为您切换至演示数据)"
                    )
                )
            }

            val nowDto = nowRes.now
            val now = WeatherNow(
                obsTime = nowDto.obsTime ?: "",
                temp = nowDto.temp,
                feelsLike = nowDto.feelsLike,
                icon = nowDto.icon,
                text = nowDto.text,
                wind360 = nowDto.wind360,
                windDir = nowDto.windDir,
                windScale = nowDto.windScale,
                windSpeed = nowDto.windSpeed,
                humidity = nowDto.humidity,
                precip = nowDto.precip,
                pressure = nowDto.pressure,
                vis = nowDto.vis,
                cloud = nowDto.cloud,
                dew = nowDto.dew
            )

            val hourlyRes = hourlyDeferred.await()
            val hourly = hourlyRes?.hourly?.map {
                HourlyForecast(
                    fxTime = it.fxTime,
                    temp = it.temp,
                    icon = it.icon,
                    text = it.text,
                    windDir = it.windDir,
                    windScale = it.windScale,
                    pop = it.pop,
                    humidity = it.humidity
                )
            } ?: generateFallbackHourly(now.temp.toIntOrNull() ?: 22, now.icon, now.text)

            val dailyRes = dailyDeferred.await()
            val daily = dailyRes?.daily?.map {
                DailyForecast(
                    fxDate = it.fxDate,
                    tempMax = it.tempMax,
                    tempMin = it.tempMin,
                    iconDay = it.iconDay,
                    textDay = it.textDay,
                    iconNight = it.iconNight,
                    textNight = it.textNight,
                    windDirDay = it.windDirDay,
                    windScaleDay = it.windScaleDay,
                    humidity = it.humidity,
                    precip = it.precip,
                    uvIndex = it.uvIndex
                )
            } ?: generateFallbackDaily(now.temp.toIntOrNull() ?: 22)

            val airRes = airDeferred.await()
            val air = airRes?.now?.let {
                AirQuality(
                    aqi = it.aqi,
                    level = it.level,
                    category = it.category,
                    pm10 = it.pm10,
                    pm2p5 = it.pm2p5,
                    no2 = it.no2,
                    so2 = it.so2,
                    co = it.co,
                    o3 = it.o3
                )
            }

            val indicesRes = indicesDeferred.await()
            val indices = indicesRes?.daily?.map {
                WeatherIndex(
                    type = it.type,
                    name = it.name,
                    level = it.level,
                    category = it.category,
                    text = it.text
                )
            } ?: defaultIndices()

            val warningRes = warningDeferred.await()
            val warnings = warningRes?.warning?.map {
                WeatherWarning(
                    id = it.id,
                    title = it.title,
                    typeName = it.typeName,
                    level = it.level,
                    pubTime = it.pubTime,
                    text = it.text
                )
            } ?: emptyList()

            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val updateFormatted = "更新于 ${sdf.format(Date())}"

            val result = FullWeatherData(
                location = location,
                now = now,
                hourly = hourly,
                daily = daily,
                air = air,
                indices = indices,
                warnings = warnings,
                updateTimeFormatted = updateFormatted,
                isDemoData = false,
                isFromCache = false
            )
            saveCachedWeatherData(result)
            Result.success(result)
        } catch (e: Exception) {
            val cached = getCachedWeatherData(location.id)
            if (cached != null) {
                Result.success(cached)
            } else {
                Result.success(
                    generateMockWeatherData(
                        location,
                        demoNotice = "网络连接异常，已为您载入离线天气"
                    )
                )
            }
        }
    }

    private fun generateMockWeatherData(location: CityLocation, demoNotice: String? = null): FullWeatherData {
        val rand = Random(location.name.hashCode())
        val baseTemp = 18 + rand.nextInt(8)
        val conditions = listOf(
            Triple("100", "晴", 10),
            Triple("101", "多云", 25),
            Triple("104", "阴", 40),
            Triple("305", "小雨", 75),
            Triple("306", "中雨", 85)
        )
        val selectedCond = conditions[rand.nextInt(conditions.size)]

        val now = WeatherNow(
            obsTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()).format(Date()),
            temp = baseTemp.toString(),
            feelsLike = (baseTemp + rand.nextInt(3) - 1).toString(),
            icon = selectedCond.first,
            text = selectedCond.second,
            windDir = listOf("东北风", "东南风", "西南风", "西北风", "南风", "北风")[rand.nextInt(6)],
            windScale = "${rand.nextInt(3) + 1}",
            windSpeed = "${rand.nextInt(15) + 5}",
            humidity = "${selectedCond.third + rand.nextInt(15)}",
            precip = if (selectedCond.second.contains("雨")) "1.5" else "0.0",
            pressure = "${1012 + rand.nextInt(8)}",
            vis = "${15 + rand.nextInt(15)}",
            cloud = "${rand.nextInt(40) + 10}"
        )

        val hourly = generateFallbackHourly(baseTemp, now.icon, now.text)
        val daily = generateFallbackDaily(baseTemp)

        val aqiValue = 25 + rand.nextInt(60)
        val (level, category) = when {
            aqiValue <= 50 -> "1" to "优"
            aqiValue <= 100 -> "2" to "良"
            else -> "3" to "轻度污染"
        }

        val air = AirQuality(
            aqi = aqiValue.toString(),
            level = level,
            category = category,
            pm2p5 = "${aqiValue / 2 + 5}",
            pm10 = "${aqiValue + 10}",
            no2 = "24",
            so2 = "6",
            co = "0.6",
            o3 = "52"
        )

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val updateFormatted = "更新于 ${sdf.format(Date())}"

        return FullWeatherData(
            location = location,
            now = now,
            hourly = hourly,
            daily = daily,
            air = air,
            indices = defaultIndices(),
            warnings = emptyList(),
            updateTimeFormatted = updateFormatted,
            isDemoData = true,
            isFromCache = false
        )
    }

    private fun generateFallbackHourly(currentTemp: Int, baseIcon: String, baseText: String): List<HourlyForecast> {
        val list = mutableListOf<HourlyForecast>()
        val cal = Calendar.getInstance()
        val hourFormat = SimpleDateFormat("HH:00", Locale.getDefault())

        for (i in 0..23) {
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val tempVariation = when (hour) {
                in 0..5 -> -4
                in 6..10 -> -1
                in 11..15 -> 3
                in 16..19 -> 1
                else -> -2
            }
            val temp = currentTemp + tempVariation
            val isNight = hour < 6 || hour >= 19
            val icon = if (isNight && baseIcon == "100") "150" else baseIcon
            list.add(
                HourlyForecast(
                    fxTime = hourFormat.format(cal.time),
                    temp = temp.toString(),
                    icon = icon,
                    text = baseText,
                    windDir = "微风",
                    windScale = "1-2",
                    pop = "${(i * 3) % 25}%",
                    humidity = "${45 + (i * 2) % 30}%"
                )
            )
            cal.add(Calendar.HOUR_OF_DAY, 1)
        }
        return list
    }

    private fun generateFallbackDaily(currentTemp: Int): List<DailyForecast> {
        val list = mutableListOf<DailyForecast>()
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        val weekFormat = SimpleDateFormat("E", Locale.CHINESE)

        val weatherTypes = listOf(
            Triple("100", "晴", "150"),
            Triple("101", "多云", "151"),
            Triple("104", "阴", "104"),
            Triple("305", "小雨", "305"),
            Triple("100", "晴", "150"),
            Triple("101", "多云", "151"),
            Triple("306", "中雨", "306")
        )

        for (i in 0..6) {
            val dateStr = if (i == 0) "今天" else if (i == 1) "明天" else weekFormat.format(cal.time)
            val fullDate = "${dateFormat.format(cal.time)} $dateStr"
            val type = weatherTypes[i % weatherTypes.size]
            val max = currentTemp + 2 + (i % 3)
            val min = currentTemp - 4 - ((i + 1) % 3)

            list.add(
                DailyForecast(
                    fxDate = fullDate,
                    tempMax = max.toString(),
                    tempMin = min.toString(),
                    iconDay = type.first,
                    textDay = type.second,
                    iconNight = type.third,
                    textNight = type.second,
                    windDirDay = "东风",
                    windScaleDay = "2级",
                    humidity = "${40 + (i * 5) % 40}",
                    uvIndex = "${3 + (i % 4)}"
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    private fun defaultIndices(): List<WeatherIndex> {
        return listOf(
            WeatherIndex(
                type = "3",
                name = "穿衣指数",
                level = "舒适",
                category = "舒适",
                text = "建议着衬衫、薄单裤等春秋过渡装。体弱者宜着套装。"
            ),
            WeatherIndex(
                type = "1",
                name = "运动指数",
                level = "适宜",
                category = "适宜",
                text = "天气较好，适宜在户外进行散步、慢跑等健身运动。"
            ),
            WeatherIndex(
                type = "5",
                name = "紫外线指数",
                level = "中等",
                category = "中等",
                text = "涂擦SPF大于15、PA+防晒护肤品，外出避开正午烈日。"
            ),
            WeatherIndex(
                type = "2",
                name = "洗车指数",
                level = "较适宜",
                category = "较适宜",
                text = "较适宜洗车，未来一天无雨，清洁后可保持较长时间。"
            )
        )
    }
}
