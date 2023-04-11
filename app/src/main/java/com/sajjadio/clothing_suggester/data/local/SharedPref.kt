package com.sajjadio.clothing_suggester.data.local

interface SharedPref {
    fun addImage(images: Pair<String, String>)
    fun getImage(key: String): MutableSet<String>?
}