package com.sajjadio.clothing_suggester.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.sajjadio.clothing_suggester.R
import com.sajjadio.clothing_suggester.data.model.Daily
import com.sajjadio.clothing_suggester.data.model.WeatherResponse
import com.sajjadio.clothing_suggester.databinding.ItemCurrentWeatherBinding
import com.sajjadio.clothing_suggester.databinding.ListDailyWeatherBinding
import com.sajjadio.clothing_suggester.utils.ParentItem
import com.sajjadio.clothing_suggester.utils.getCelsiusTemperature
import com.sajjadio.clothing_suggester.utils.loadImage

class ParentAdapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ParentAdapter.ParentViewHolder>() {

    private val nestedItem = mutableListOf<ParentItem>()

    @SuppressLint("NotifyDataSetChanged")
    fun addNestedItem(newItem: ParentItem) {
        nestedItem.apply {
            add(newItem)
            sortBy {
                it.priority
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            CURRENT_Weather -> {
                val view = ItemCurrentWeatherBinding.inflate(inflater, parent, false)
                CurrentWeatherViewHolder(view)
            }
            else -> {
                val view = ListDailyWeatherBinding.inflate(inflater, parent, false)
                DailyWeatherViewHolder(view)
            }
        }
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        when (holder) {
            is CurrentWeatherViewHolder -> holder.bindItem(nestedItem[position].item)
            is DailyWeatherViewHolder -> holder.bindItem(nestedItem[position].item)
        }
    }

    override fun getItemCount() = nestedItem.size

    override fun getItemViewType(position: Int): Int {
        return when (nestedItem[position]) {
            is ParentItem.CurrentWeather -> CURRENT_Weather
            is ParentItem.DailyWeather -> DAILY_WEATHER
        }
    }

    abstract class ParentViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bindItem(item: Any)
    }

    inner class CurrentWeatherViewHolder(val binding: ItemCurrentWeatherBinding) :
        ParentViewHolder(binding) {
        @SuppressLint("SetTextI18n")
        override fun bindItem(item: Any) {
            val weather = item as WeatherResponse
            val temp = weather.main.temp.getCelsiusTemperature()
            with(binding) {
                textViewCity.text = weather.name
                textViewTemp.text = "$temp°"
                textViewMaxTemp.text = "${weather.main.temp_max.getCelsiusTemperature()}°"
                textViewMinTemp.text = "${weather.main.temp_min.getCelsiusTemperature()}°"
                textViewDescriptionWeather.text = weather.weather.first().main

                weather.weather.forEach {
                    val iconUrl = "https://openweathermap.org/img/wn/${it.icon}@2x.png"
                    imageViewWeatherIcon.loadImage(iconUrl)
                }

                listener.getCurrentDayTemp(temp)
                setupSuggesterImage(listener.refreshSuggesterImage())
                buttonAddImage.setOnClickListener {
                    listener.addImage()
                }
                buttonRefreshSuggesterImage.setOnClickListener {
                    setupSuggesterImage(listener.refreshSuggesterImage())
                }
            }
        }

        private fun setupSuggesterImage(bitmap: Bitmap?) {
            with(binding.imageViewCloths) {
                bitmap?.let {
                    setImageBitmap(it)
                } ?: setImageResource(R.drawable.ic_launcher_background)
            }
        }
    }

    inner class DailyWeatherViewHolder(val binding: ListDailyWeatherBinding) :
        ParentViewHolder(binding) {
        override fun bindItem(item: Any) {
            with(binding) {
                val adapter = DailyWeatherAdapter(item as List<Daily>)
                recyclerViewDailyWeather.adapter = adapter
            }
        }
    }

    private companion object {
        const val CURRENT_Weather = R.layout.item_current_weather
        const val DAILY_WEATHER = R.layout.list_daily_weather
    }

}