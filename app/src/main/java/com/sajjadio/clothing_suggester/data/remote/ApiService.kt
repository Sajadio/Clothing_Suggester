package com.sajjadio.clothing_suggester.data.remote

import com.sajjadio.clothing_suggester.data.model.WeatherResponse

interface ApiService {
    fun getWeatherResponse(onSuccess: (Boolean) -> Unit, function: (WeatherResponse) -> Unit)
    fun getDailyWeatherResponse(onSuccess: (Boolean) -> Unit, function: (WeatherResponse) -> Unit)
}