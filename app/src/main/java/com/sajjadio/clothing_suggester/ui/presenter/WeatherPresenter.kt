package com.sajjadio.clothing_suggester.ui.presenter

import com.sajjadio.clothing_suggester.data.remote.ApiService

class WeatherPresenter(
    private val view: WeatherView,
    private val apiService: ApiService
) {

    fun getCurrentWeatherResponse() {
        apiService.getWeatherResponse(::responseCurrentWeather) { response ->
            view.getCurrentWeatherResponse(response)
        }
        apiService.getDailyWeatherResponse(::responseDailyWeather) { response ->
            view.getDailyWeatherResponse(response.daily)
        }
    }

    private fun responseCurrentWeather(isSuccess: Boolean) {
        view.checkResponseCurrentWeather(isSuccess)
    }

    private fun responseDailyWeather(isSuccess: Boolean) {
        view.checkResponseDailyWeather(isSuccess)
    }
}