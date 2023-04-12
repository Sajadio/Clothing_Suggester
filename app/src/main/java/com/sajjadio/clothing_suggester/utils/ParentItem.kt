package com.sajjadio.clothing_suggester.utils

import com.sajjadio.clothing_suggester.data.model.Daily
import com.sajjadio.clothing_suggester.data.model.WeatherResponse

sealed class ParentItem(val priority: Int,val item:Any) {
    class CurrentWeather(weatherResponse: WeatherResponse) : ParentItem(0,weatherResponse)
    class DailyWeather(daysWeather: List<Daily>) : ParentItem(1,daysWeather)
}
