package com.example.timerboxing.presentation.format

fun formatDuration(seconds: Int): String =
    "${(seconds / 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}"