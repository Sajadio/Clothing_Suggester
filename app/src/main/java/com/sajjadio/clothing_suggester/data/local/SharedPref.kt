package com.sajjadio.clothing_suggester.data.local

interface SharedPref {
    fun saveImage(images: Pair<String, String>)
    fun getImage(key: String): List<String>

}