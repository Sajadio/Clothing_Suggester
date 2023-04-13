package com.sajjadio.clothing_suggester.data.local

import android.content.Context

class SharedPrefImpl(
    context: Context
) : SharedPref {

    private val prefs = context.getSharedPreferences(CLOTHS_PREF, Context.MODE_PRIVATE)

    private companion object {
        private const val CLOTHS_PREF = "cloths pref"
    }

    override fun saveImage(images: Pair<String, String>) {
        val oldValue = getImage(images.first).toMutableSet()
        oldValue.add(images.second)
        prefs.edit().apply {
            putStringSet(images.first, oldValue)
            apply()
        }
    }


    override fun getImage(key: String): List<String> {
        return prefs.getStringSet(key, emptySet())?.toMutableSet()?.toList() ?: emptyList()
    }
}