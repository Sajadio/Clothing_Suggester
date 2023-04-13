package com.sajjadio.clothing_suggester.ui.presenter

import com.sajjadio.clothing_suggester.data.model.Daily
import com.sajjadio.clothing_suggester.data.model.WeatherResponse

interface WeatherView {

    fun showImage(images: List<String>)

    fun loadDataCurrentWeatherResponse(weatherResponse: WeatherResponse)
    fun loadDataDailyWeatherResponse(daysWeather: List<Daily>)
    fun checkResponseCurrentWeather(isSuccess: Boolean)
    fun checkResponseDailyWeather(isSuccess: Boolean)

}