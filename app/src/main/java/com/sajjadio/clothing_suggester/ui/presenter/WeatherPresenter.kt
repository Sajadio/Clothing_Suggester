package com.sajjadio.clothing_suggester.ui.presenter

import com.sajjadio.clothingsuggester.domain.Repository

class WeatherPresenter(
    private val view: WeatherView,
    private val repository: Repository
    ) {

    fun getWeatherResponse(){
        repository.getWeatherResponse{ dailyWeather ->
            view.getWeatherResponse(dailyWeather)
        }
    }
}