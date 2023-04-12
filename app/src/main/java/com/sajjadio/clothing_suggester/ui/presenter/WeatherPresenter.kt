package com.sajjadio.clothing_suggester.ui.presenter

import com.sajjadio.clothing_suggester.data.remote.ApiService

class WeatherPresenter(
    private val view: WeatherView,
    private val apiService: ApiService
    ) {

    fun getCurrentWeatherResponse(){
        apiService.getWeatherResponse{ response ->
            view.getCurrentWeatherResponse(response)
        }
        apiService.getDailyWeatherResponse{ response ->
            view.getDailyWeatherResponse(response.daily)
        }
    }
}