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
            .scheme(SCHEME)
            .host(HOST)
            .addPathSegments(PATH_SEGMENTS_CURRENT)
            .addQueryParameter(APP_ID, API_KEY)
            .addQueryParameter(LAN, LATITUDE)
            .addQueryParameter(LON, LONGITUDE)
            .build()
    }

    private fun buildDailyWeatherUrl(): HttpUrl {
        return HttpUrl.Builder()
            .scheme(SCHEME)
            .host(HOST)
            .addPathSegments(PATH_SEGMENTS_DAILY)
            .addQueryParameter(APP_ID, API_KEY)
            .addQueryParameter(LAN, LATITUDE)
            .addQueryParameter(LON, LONGITUDE)
            .addQueryParameter(HOURLY, EXCLUDE)
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
        const val LATITUDE = "30.5257657"
        const val LONGITUDE = "47.773797"
        const val LAN = "lat"
        const val LON = "lot"
        const val APP_ID = "lot"
        const val EXCLUDE = "hourly"
        const val HOURLY = "hourly"
        const val SCHEME = "scheme"
        const val HOST = "api.openweathermap.org"
        const val PATH_SEGMENTS_DAILY = "data/2.5/onecall"
        const val PATH_SEGMENTS_CURRENT = "data/2.5/weather"
    }

}