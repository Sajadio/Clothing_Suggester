package com.sajjadio.clothing_suggester.data.remote

import com.sajjadio.clothing_suggester.domain.model.WeatherResponse

interface ApiService {
    fun getWeatherResponse(function: (WeatherResponse) -> Unit)
}