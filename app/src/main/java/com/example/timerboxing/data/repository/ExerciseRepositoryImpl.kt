package com.example.timerboxing.data.repository

import com.example.timerboxing.data.remote.ExerciseApi
import com.example.timerboxing.data.remote.ExerciseDto
import com.example.timerboxing.domain.model.Exercise
import com.example.timerboxing.domain.repository.ExerciseRepository

class ExerciseRepositoryImpl(private val api: ExerciseApi) : ExerciseRepository {
    override suspend fun getItems(): List<Exercise> {
        val exercises = mutableListOf<Exercise>()
        var offset = 0
        do {
            val page = api.getExercises(limit = 100, offset = offset)
            check(page.results.isNotEmpty() || page.next == null) {
                "The exercise API returned an empty page with more results remaining."
            }
            exercises.addAll(page.results.map { it.toModel() })
            offset += page.results.size
        } while (page.next != null)
        return exercises.distinctBy { it.id }
    }
}

private fun ExerciseDto.toModel() = Exercise(
    id = id,
    uuid = uuid,
    categoryId = category,
    muscleIds = muscles.toList(),
    equipmentIds = equipment.toList()
)
