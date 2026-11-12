package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface QWeatherApiService {
    @GET
    suspend fun lookupCity(
        @Url url: String,
        @Query("location") location: String,
        @Query("key") key: String,
        @Query("range") range: String? = null,
        @Query("number") number: Int = 10
    ): CityLookupResponse

    @GET
    suspend fun getWeatherNow(
        @Url url: String,
        @Query("location") location: String,
        @Query("key") key: String
    ): WeatherNowResponse

    @GET
    suspend fun getWeather24h(
        @Url url: String,
        @Query("location") location: String,
        @Query("key") key: String
    ): WeatherHourlyResponse

    @GET
    suspend fun getWeather7d(
        @Url url: String,
        @Query("location") location: String,
        @Query("key") key: String
    ): WeatherDailyResponse

    @GET
    suspend fun getAirNow(
        @Url url: String,
        @Query("location") location: String,
        @Query("key") key: String
    ): AirNowResponse

    @GET
    suspend fun getIndices1d(
        @Url url: String,
        @Query("location") location: String,
        @Query("key") key: String,
        @Query("type") type: String = "1,2,3,5"
    ): IndicesResponse

    @GET
    suspend fun getWeatherWarning(
        @Url url: String,
        @Query("location") location: String,
        @Query("key") key: String
    ): WarningResponse
}

object NetworkClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val apiService: QWeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://devapi.qweather.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(QWeatherApiService::class.java)
    }
}
