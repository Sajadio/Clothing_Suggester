package com.sajjadio.clothing_suggester.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

fun ImageView.loadImage(url: String) {
    Glide.with(this).load(url).into(this)
}

fun Double.getCelsiusTemperature(): Int {
    return this.minus(273.15).toInt()
}



fun Long.getHourAndMinuteFromTimestamp(): String {
    val date = Date(this * 1000)
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = dateFormat.format(date)
    val timeParts = formattedTime.split(":")
    val hour = timeParts[0]
    val minute = timeParts[1]
    return "$hour:$minute"
}
