package com.sajjadio.clothing_suggester.data

import com.sajjadio.clothingsuggester.domain.Repository
import com.sajjadio.clothing_suggester.domain.model.WeatherResponse
import com.sajjadio.clothing_suggester.data.remote.ApiService

class RepositoryImpl(
    private val api: ApiService
) : Repository {

    override fun getWeatherResponse(function: (WeatherResponse) -> Unit) {
        api.getWeatherResponse(function)
    }
}