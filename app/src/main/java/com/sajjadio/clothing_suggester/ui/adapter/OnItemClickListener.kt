package com.sajjadio.clothing_suggester.ui.adapter

import android.graphics.Bitmap

interface OnItemClickListener {
    fun getCurrentDayTemp(temp:Int)
    fun onAddImage()
    fun onRefreshSuggesterImage():Bitmap?
}