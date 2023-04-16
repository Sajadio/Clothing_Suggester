package com.sajjadio.clothing_suggester.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.util.Util
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

fun String.mapStringToBitmap(): Bitmap? {
    val imageBytes = Base64.decode(this, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

@SuppressLint("Recycle")
fun Context.mapUriToString(uri: Uri): String {
    val inputStream = contentResolver.openInputStream(uri)
    val imageBytes = inputStream?.readBytes()
    return Base64.encodeToString(imageBytes, Base64.DEFAULT)
}
