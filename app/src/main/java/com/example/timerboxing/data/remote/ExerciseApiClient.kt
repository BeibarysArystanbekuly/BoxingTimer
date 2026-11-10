package com.example.timerboxing.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ExerciseApiClient {
    val api: ExerciseApi by lazy {
        Retrofit.Builder()
            .baseUrl(ExerciseApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExerciseApi::class.java)
    }
}
