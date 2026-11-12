package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppThemeMode
import com.example.model.WeatherUiState
import com.example.ui.components.*
import com.example.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    onRequestLocationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLocating by viewModel.isLocating.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    val showFirstLaunchDialog by viewModel.showFirstLaunchDialog.collectAsState()
    val showSearchDialog by viewModel.showSearchDialog.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    val showApiKeyDialog by viewModel.showApiKeyDialog.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val isCommercial by viewModel.isCommercial.collectAsState()

    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissUserMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentCity.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (currentCity.isCurrentLocation) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.NearMe,
                                    contentDescription = "当前定位",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = if (currentCity.adm1.isNotEmpty() && currentCity.adm1 != currentCity.name)
                                "${currentCity.adm1} ${currentCity.adm2} · ${currentCity.id}" else "${currentCity.country} · ${currentCity.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Quick Search Action Button
                    IconButton(
                        onClick = { viewModel.openSearch() },
                        modifier = Modifier.testTag("top_bar_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索城市"
                        )
                    }

                    // Adaptive Theme Mode Toggle
                    IconButton(
                        onClick = {
                            val nextMode = when (themeMode) {
                                AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
                                AppThemeMode.LIGHT -> AppThemeMode.DARK
                                AppThemeMode.DARK -> AppThemeMode.SYSTEM
                            }
                            viewModel.setThemeMode(nextMode)
                        },
                        modifier = Modifier.testTag("top_bar_theme_toggle")
                    ) {
                        val icon = when (themeMode) {
                            AppThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                            AppThemeMode.LIGHT -> Icons.Default.LightMode
                            AppThemeMode.DARK -> Icons.Default.DarkMode
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "切换深浅色主题模式"
                        )
                    }

                    // Location Action Button
                    IconButton(
                        onClick = {
                            onRequestLocationPermission()
                            viewModel.locateAndFetchWeather()
                        },
                        modifier = Modifier.testTag("top_bar_location_button")
                    ) {
                        if (isLocating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "获取当前定位天气"
                            )
                        }
                    }

                    // Settings / API Key Action Button (Can be modified anytime)
                    IconButton(
                        onClick = { viewModel.openApiKeyDialog() },
                        modifier = Modifier.testTag("top_bar_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "配置和风天气Key"
                        )
                    }

                    // Refresh Button
                    IconButton(
                        onClick = { viewModel.refreshWeather() },
                        modifier = Modifier.testTag("top_bar_refresh_button")
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "刷新天气"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openSearch() },
                icon = { Icon(Icons.Default.Search, contentDescription = null) },
                text = { Text("搜索城市") },
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("floating_search_button")
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is WeatherUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "正在获取和风天气数据...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is WeatherUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "天气获取失败",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { viewModel.refreshWeather() }) {
                                        Text("重试")
                                    }
                                    OutlinedButton(onClick = { viewModel.openApiKeyDialog() }) {
                                        Text("配置Key")
                                    }
                                }
                            }
                        }
                    }
                }

                is WeatherUiState.Success -> {
                    val weather = state.data
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Quick Search Bar Strip
                        Surface(
                            onClick = { viewModel.openSearch() },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quick_search_bar_trigger")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "搜索",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "输入城市自动匹配City ID (如：杭州、浦东、三亚)...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "搜索",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Background Seamless Feature: Severe Weather Warning Banner
                        if (weather.warnings.isNotEmpty()) {
                            SevereWeatherWarningBanner(warnings = weather.warnings)
                        }

                        // Background Seamless Feature: Instant Local Cache Badge
                        if (weather.isFromCache) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "无感秒开：展示本地高保真缓存，后台静默联网同步中",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        // 1. Hero Weather Card
                        HeroWeatherHeader(
                            data = weather,
                            onConfigureKeyClick = { viewModel.openApiKeyDialog() }
                        )

                        // 2. 24-Hour Forecast
                        HourlyForecastCard(hourly = weather.hourly)

                        // 3. 7-Day Forecast
                        DailyForecastCard(daily = weather.daily)

                        // 4. Air Quality
                        AirQualityCard(air = weather.air)

                        // 5. Weather Metrics Grid
                        WeatherMetricsGrid(now = weather.now)

                        // 6. Life Indices
                        LifeIndicesSection(indices = weather.indices)

                        // Footer with Edge-to-Edge Navigation Padding
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "和风天气 QWeather API · Material Design 3 · Edge-to-Edge",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }

                        // Bottom spacing for FAB and navigation bar
                        Spacer(modifier = Modifier.height(84.dp))
                    }
                }
            }
        }
    }

    // 1. First-Launch Onboarding Dialog (Prompt to input API key and can be modified anytime)
    if (showFirstLaunchDialog) {
        FirstLaunchWelcomeDialog(
            initialKey = apiKey,
            onComplete = { key, commercial ->
                viewModel.completeFirstLaunch(key, commercial)
            }
        )
    }

    // 2. City Search Dialog (Input city name automatically converted to City ID)
    if (showSearchDialog) {
        CitySearchDialog(
            searchQuery = searchQuery,
            searchResults = searchResults,
            isSearching = isSearching,
            recentSearches = recentSearches,
            popularCities = viewModel.popularCities,
            onQueryChange = { viewModel.onSearchQueryChange(it) },
            onSearchSubmit = { viewModel.performSearch(it) },
            onCitySelected = { viewModel.selectCity(it) },
            onClearRecent = { viewModel.clearRecentSearches() },
            onDismiss = { viewModel.closeSearch() }
        )
    }

    // 3. API Key & Host & Theme Configuration Dialog (Available anytime via TopBar Key button)
    if (showApiKeyDialog) {
        ApiKeySettingsDialog(
            currentApiKey = apiKey,
            currentIsCommercial = isCommercial,
            currentThemeMode = themeMode,
            onThemeChange = { viewModel.setThemeMode(it) },
            onSave = { key, commercial ->
                viewModel.saveApiKey(key, commercial)
            },
            onDismiss = { viewModel.closeApiKeyDialog() }
        )
    }
}
