package com.sajjadio.clothing_suggester.ui.presenter

import com.sajjadio.clothing_suggester.domain.model.WeatherResponse

interface WeatherView {
    fun getWeatherResponse(weatherResponse: WeatherResponse)
}