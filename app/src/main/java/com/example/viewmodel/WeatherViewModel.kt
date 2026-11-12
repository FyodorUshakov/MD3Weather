package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.WeatherRepository
import com.example.data.location.LocationHelper
import com.example.model.AppThemeMode
import com.example.model.CityLocation
import com.example.model.WeatherUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeatherRepository(application)
    private val locationHelper = LocationHelper(application)

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _currentCity = MutableStateFlow(
        repository.getLastCity() ?: WeatherRepository.POPULAR_CITIES[0]
    )
    val currentCity: StateFlow<CityLocation> = _currentCity.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Theme mode state
    private val _themeMode = MutableStateFlow(repository.themeMode)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    // First launch onboarding state
    private val _showFirstLaunchDialog = MutableStateFlow(repository.isFirstLaunch)
    val showFirstLaunchDialog: StateFlow<Boolean> = _showFirstLaunchDialog.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<CityLocation>>(emptyList())
    val searchResults: StateFlow<List<CityLocation>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(repository.getRecentSearches())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    val popularCities = WeatherRepository.POPULAR_CITIES

    // Dialog & sheet states
    private val _showSearchDialog = MutableStateFlow(false)
    val showSearchDialog: StateFlow<Boolean> = _showSearchDialog.asStateFlow()

    private val _showApiKeyDialog = MutableStateFlow(false)
    val showApiKeyDialog: StateFlow<Boolean> = _showApiKeyDialog.asStateFlow()

    private val _apiKey = MutableStateFlow(repository.apiKey)
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _isCommercial = MutableStateFlow(repository.isCommercialHost)
    val isCommercial: StateFlow<Boolean> = _isCommercial.asStateFlow()

    private val _isLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = _isLocating.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadWeatherWithSeamlessCache(_currentCity.value)
    }

    /**
     * Seamless Background Feature:
     * 1. Immediately renders local cached weather with 0ms delay.
     * 2. Silently fetches fresh live QWeather data in background and updates UI seamlessly.
     */
    fun loadWeatherWithSeamlessCache(city: CityLocation) {
        _currentCity.value = city
        repository.saveLastCity(city)

        // Step 1: 0ms Instant Cache Render
        val cached = repository.getCachedWeatherData(city.id)
        if (cached != null) {
            _uiState.value = WeatherUiState.Success(cached)
        } else {
            _uiState.value = WeatherUiState.Loading
        }

        // Step 2: Background Seamless Revalidation
        viewModelScope.launch {
            val result = repository.fetchWeather(city)
            result.onSuccess {
                _uiState.value = WeatherUiState.Success(it)
            }.onFailure {
                if (_uiState.value !is WeatherUiState.Success) {
                    _uiState.value = WeatherUiState.Error(it.localizedMessage ?: "获取天气失败")
                }
            }
        }
    }

    fun refreshWeather() {
        val city = _currentCity.value
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = repository.fetchWeather(city)
            result.onSuccess {
                _uiState.value = WeatherUiState.Success(it)
                _userMessage.value = "天气数据已刷新"
            }.onFailure {
                _userMessage.value = "刷新失败: ${it.localizedMessage}"
            }
            _isRefreshing.value = false
        }
    }

    fun locateAndFetchWeather() {
        viewModelScope.launch {
            _isLocating.value = true
            val loc = locationHelper.getCurrentLocation()
            if (loc != null) {
                val city = repository.reverseGeocode(loc.longitude, loc.latitude)
                if (city != null) {
                    loadWeatherWithSeamlessCache(city)
                    _userMessage.value = "已自动匹配城市ID并定位至: ${city.name} (${city.id})"
                } else {
                    val latStr = String.format("%.2f", loc.latitude)
                    val lonStr = String.format("%.2f", loc.longitude)
                    val gpsCity = CityLocation(
                        id = "${lonStr},${latStr}",
                        name = "当前位置",
                        adm2 = "GPS定位",
                        adm1 = "经纬度 (${lonStr}, ${latStr})",
                        country = "中国",
                        lat = latStr,
                        lon = lonStr,
                        isCurrentLocation = true
                    )
                    loadWeatherWithSeamlessCache(gpsCity)
                    _userMessage.value = "已获取GPS定位坐标并加载天气"
                }
            } else {
                _userMessage.value = "未能获取定位信息，请检查定位权限与GPS开关"
            }
            _isLocating.value = false
        }
    }

    fun openSearch() {
        _showSearchDialog.value = true
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _recentSearches.value = repository.getRecentSearches()
    }

    fun closeSearch() {
        _showSearchDialog.value = false
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.trim().isEmpty()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            _isSearching.value = true
            val results = repository.searchCities(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    /**
     * Requirement: User inputs city name, system automatically converts it to official City ID!
     */
    fun performSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        searchJob?.cancel()

        viewModelScope.launch {
            _isSearching.value = true
            repository.addRecentSearch(trimmed)
            _recentSearches.value = repository.getRecentSearches()

            // Resolve input string to authoritative City ID (QWeather GeoAPI / built-in map)
            val resolvedCity = repository.resolveCityAndGetId(trimmed)
            _isSearching.value = false
            _showSearchDialog.value = false
            _userMessage.value = "已自动匹配城市ID: ${resolvedCity.name} -> ${resolvedCity.id}"
            loadWeatherWithSeamlessCache(resolvedCity)
        }
    }

    fun selectCity(city: CityLocation) {
        repository.addRecentSearch(city.name)
        _recentSearches.value = repository.getRecentSearches()
        _showSearchDialog.value = false
        _userMessage.value = "切换至城市: ${city.name} (城市ID: ${city.id})"
        loadWeatherWithSeamlessCache(city)
    }

    fun clearRecentSearches() {
        repository.clearRecentSearches()
        _recentSearches.value = emptyList()
    }

    fun openApiKeyDialog() {
        _apiKey.value = repository.apiKey
        _isCommercial.value = repository.isCommercialHost
        _showApiKeyDialog.value = true
    }

    fun closeApiKeyDialog() {
        _showApiKeyDialog.value = false
    }

    fun saveApiKey(key: String, isCommercial: Boolean) {
        repository.apiKey = key
        repository.isCommercialHost = isCommercial
        _apiKey.value = key
        _isCommercial.value = isCommercial
        _showApiKeyDialog.value = false
        _userMessage.value = if (key.isNotBlank()) "和风天气Key已更新，正在重新获取天气..." else "已清空Key，将使用内置演示数据"
        loadWeatherWithSeamlessCache(_currentCity.value)
    }

    fun completeFirstLaunch(key: String, isCommercial: Boolean) {
        repository.isFirstLaunch = false
        repository.apiKey = key
        repository.isCommercialHost = isCommercial
        _apiKey.value = key
        _isCommercial.value = isCommercial
        _showFirstLaunchDialog.value = false
        _userMessage.value = if (key.isNotBlank()) "和风天气Key已配置，欢迎使用！" else "已进入演示模式，随时可在顶栏修改Key"
        loadWeatherWithSeamlessCache(_currentCity.value)
    }

    fun setThemeMode(mode: AppThemeMode) {
        repository.themeMode = mode
        _themeMode.value = mode
        _userMessage.value = when (mode) {
            AppThemeMode.SYSTEM -> "已切换为：跟随系统深浅色"
            AppThemeMode.LIGHT -> "已切换为：浅色模式"
            AppThemeMode.DARK -> "已切换为：深色模式"
        }
    }

    fun dismissUserMessage() {
        _userMessage.value = null
    }
}
