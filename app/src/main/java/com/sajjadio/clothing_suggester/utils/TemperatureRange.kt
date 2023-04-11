package com.sajjadio.clothing_suggester.utils

data class TemperatureRange(
    val hot: IntRange = 30..50,
    val moderate: IntRange = 21..27,
    val cold: IntRange = 10..20
)
