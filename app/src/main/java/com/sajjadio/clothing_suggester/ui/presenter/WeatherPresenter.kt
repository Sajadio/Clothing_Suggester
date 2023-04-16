package com.sajjadio.clothing_suggester.ui.presenter

import com.sajjadio.clothing_suggester.data.local.SharedPref
import com.sajjadio.clothing_suggester.data.remote.ApiService

class WeatherPresenter(
    private val view: WeatherView,
    private val apiService: ApiService,
    private val sharedPref: SharedPref
) {

    fun saveImage(images: Pair<String, String>) {
        sharedPref.saveImage(images)
    }

    fun showImage(key: String) {
        view.showImage(sharedPref.getImage(key))
    }

    fun loadData() {
        apiService.getWeatherResponse(::responseCurrentWeather) { response ->
            view.loadDataCurrentWeatherResponse(response)
        }
        apiService.getDailyWeatherResponse(::responseDailyWeather) { response ->
            view.loadDataDailyWeatherResponse(response.daily)
        }
    }

    private fun responseCurrentWeather(isSuccess: Boolean) {
        view.checkResponseCurrentWeather(isSuccess)
    }

    private fun responseDailyWeather(isSuccess: Boolean) {
        view.checkResponseDailyWeather(isSuccess)
    }
}