package com.sajjadio.clothingsuggester.domain

import com.sajjadio.clothing_suggester.domain.model.WeatherResponse

interface Repository {
    fun getWeatherResponse(function: (WeatherResponse) -> Unit)
}