package com.sajjadio.clothing_suggester.ui.presenter

import com.sajjadio.clothing_suggester.data.model.Daily
import com.sajjadio.clothing_suggester.data.model.WeatherResponse

interface WeatherView {
    fun getCurrentWeatherResponse(weatherResponse: WeatherResponse)
    fun getDailyWeatherResponse(daysWeather: List<Daily>)

}