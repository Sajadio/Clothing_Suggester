package com.sajjadio.clothing_suggester.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sajjadio.clothing_suggester.data.model.Daily
import com.sajjadio.clothing_suggester.databinding.ItemWeatherCardBinding
import com.sajjadio.clothing_suggester.utils.getCelsiusTemperature
import com.sajjadio.clothing_suggester.utils.getHourAndMinuteFromTimestamp
import com.sajjadio.clothing_suggester.utils.loadImage

class DailyWeatherAdapter(
    private val items: List<Daily>
) : RecyclerView.Adapter<DailyWeatherAdapter.DailyWeatherViewHolder>() {

    inner class DailyWeatherViewHolder(val binding: ItemWeatherCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyWeatherViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = ItemWeatherCardBinding.inflate(inflater, parent, false)
        return DailyWeatherViewHolder(view)
    }

    override fun getItemCount() = items.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DailyWeatherViewHolder, position: Int) {
        val daily = items[position]
        with(holder.binding) {
            textViewTempWeather.text = "${daily.temp.day.getCelsiusTemperature()}°"
            textViewTime.text = daily.dt.toLong().getHourAndMinuteFromTimestamp()
            daily.weather.forEach {
                val iconUrl = "https://openweathermap.org/img/wn/${it.icon}@2x.png"
                imageViewWeatherIcon.loadImage(iconUrl)
            }
        }
    }
}