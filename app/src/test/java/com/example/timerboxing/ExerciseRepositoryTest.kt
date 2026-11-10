package com.example.timerboxing

import com.example.timerboxing.data.remote.ExerciseApi
import com.example.timerboxing.data.remote.ExerciseDto
import com.example.timerboxing.data.remote.ExercisePageDto
import com.example.timerboxing.data.repository.ExerciseRepositoryImpl
import com.example.timerboxing.domain.model.Exercise
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test

class ExerciseRepositoryTest {
    @Test
    fun mapsAllPagesToDomainModels() = runTest {
        val offsets = mutableListOf<Int>()
        val api = object : ExerciseApi {
            override suspend fun getExercises(limit: Int, offset: Int): ExercisePageDto {
                assertEquals(100, limit)
                offsets.add(offset)
                return ExercisePageDto(
                    count = 2,
                    next = if (offset == 0) "https://wger.de/api/v2/exercise/?offset=1" else null,
                    previous = null,
                    results = listOf(ExerciseDto(offset + 1, "uuid-$offset", 4, listOf(2, 3), listOf(7)))
                )
            }
        }

        val items = ExerciseRepositoryImpl(api).getItems()

        assertEquals(listOf(0, 1), offsets)
        assertEquals(
            listOf(
                Exercise(1, "uuid-0", 4, listOf(2, 3), listOf(7)),
                Exercise(2, "uuid-1", 4, listOf(2, 3), listOf(7))
            ),
            items
        )
    }

    @Test
    fun propagatesApiFailure() = runTest {
        val failure = IOException("Network unavailable")
        val api = object : ExerciseApi {
            override suspend fun getExercises(limit: Int, offset: Int): ExercisePageDto = throw failure
        }

        try {
            ExerciseRepositoryImpl(api).getItems()
            fail("Expected the API failure")
        } catch (exception: IOException) {
            assertSame(failure, exception)
        }
    }
}
