package com.sajjadio.clothing_suggester.data.remote

import com.sajjadio.clothing_suggester.data.model.WeatherResponse

interface ApiService {
    fun getWeatherResponse(function: (WeatherResponse) -> Unit)
    fun getDailyWeatherResponse(function: (WeatherResponse) -> Unit)
}