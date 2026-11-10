package com.example.timerboxing.data.remote

data class ExerciseDto(
    val id: Int,
    val uuid: String,
    val category: Int,
    val muscles: List<Int>,
    val equipment: List<Int>
)

data class ExercisePageDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<ExerciseDto>
)
