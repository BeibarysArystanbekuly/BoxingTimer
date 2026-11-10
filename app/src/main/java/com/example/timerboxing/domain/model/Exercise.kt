package com.example.timerboxing.domain.model

data class Exercise(
    val id: Int,
    val uuid: String,
    val categoryId: Int,
    val muscleIds: List<Int>,
    val equipmentIds: List<Int>
)
