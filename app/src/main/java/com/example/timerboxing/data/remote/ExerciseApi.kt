package com.example.timerboxing.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface ExerciseApi {
    @GET("exercise/")
    suspend fun getExercises(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): ExercisePageDto

    companion object {
        const val BASE_URL = "https://wger.de/api/v2/"
    }
}
