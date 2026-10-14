package com.example.timerboxing.features.workoutsettings

fun parseDuration(value: String): Int? {
    if (!value.matches(Regex("[0-9]{2}:[0-9]{2}"))) return null
    val minutes = value.substring(0, 2).toInt()
    val seconds = value.substring(3, 5).toInt()
    if (seconds > 59) return null
    return minutes * 60 + seconds
}

fun validDuration(value: String, range: IntRange): Boolean =
    parseDuration(value)?.let { it in range } == true
