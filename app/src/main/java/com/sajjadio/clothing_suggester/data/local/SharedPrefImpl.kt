package com.sajjadio.clothing_suggester.data.local

import android.content.Context

class SharedPrefImpl(
    context: Context
) : SharedPref {

    private val prefs = context.getSharedPreferences(CLOTHS_PREF, Context.MODE_PRIVATE)

    private companion object {
        private const val CLOTHS_PREF = "cloths pref"
    }

    override fun addImage(images: Pair<String, String>) {
        val oldValue = getImage(images.first)
        oldValue?.add(images.second)
        prefs.edit().apply {
            putStringSet(images.first, oldValue)
            apply()
        }
    }


    override fun getImage(key: String): MutableSet<String>? {
        return prefs.getStringSet(key, emptySet())?.toMutableSet()
    }

}