package com.sajjadio.clothing_suggester.data.remote

import android.util.Log
import com.google.gson.Gson
import com.sajjadio.clothing_suggester.data.model.WeatherResponse
import okhttp3.*
import java.io.IOException

class ApiServiceImpl : ApiService {

    private val client = OkHttpClient()

    override fun getWeatherResponse(
        onSuccess: (Boolean) -> Unit,
        function: (WeatherResponse) -> Unit
    ) {
        val client = OkHttpClient()
        val request = buildWeatherRequest()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "onFailure: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                onSuccess(response.isSuccessful)
                handleWeatherResponse(response, function)
            }
        })
    }

    override fun getDailyWeatherResponse(
        onSuccess: (Boolean) -> Unit,
        function: (WeatherResponse) -> Unit
    ) {
        val request = buildDailyWeatherRequest()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "onFailure: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                onSuccess(response.isSuccessful)
                handleWeatherResponse(response, function)
            }
        })
    }

    private fun handleWeatherResponse(response: Response, function: (WeatherResponse) -> Unit) {
        try {
            response.body?.string()?.let { jsonString ->
                val result = Gson().fromJson(jsonString, WeatherResponse::class.java)
                function(result)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Exception: ${e.message}")
        }
    }

    private fun buildWeatherRequest(): Request {
        val url = buildWeatherUrl()
        return buildRequest(url)
    }

    private fun buildDailyWeatherRequest(): Request {
        val url = buildDailyWeatherUrl()
        return buildRequest(url)
    }

    private fun buildWeatherUrl(): HttpUrl {
        return HttpUrl.Builder()
            .scheme("https")
            .host("api.openweathermap.org")
            .addPathSegments("data/2.5/weather")
            .addQueryParameter("appid", API_KEY)
            .addQueryParameter("lat", LATITUDE)
            .addQueryParameter("lon", LONGITUDE)
            .build()
    }

    private fun buildDailyWeatherUrl(): HttpUrl {
        return HttpUrl.Builder()
            .scheme("https")
            .host("api.openweathermap.org")
            .addPathSegments("data/2.5/onecall")
            .addQueryParameter("appid", API_KEY)
            .addQueryParameter("lat", LATITUDE)
            .addQueryParameter("lon", LONGITUDE)
            .addQueryParameter("hourly", EXCLUDE)
            .build()
    }

    private fun buildRequest(url: HttpUrl): Request {
        return Request.Builder()
            .url(url)
            .build()
    }

    private companion object {
        const val TAG = "sajjadio"
        const val API_KEY = "8a28304434cc3379ceb50bfe875b6602"
        const val LATITUDE = "30.5128"
        const val LONGITUDE = "47.8132"
        const val EXCLUDE = "hourly"
    }

}