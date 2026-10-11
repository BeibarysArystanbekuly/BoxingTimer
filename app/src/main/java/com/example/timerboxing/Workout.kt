package com.example.timerboxing

data class Workout(
    val id: Int,
    val name: String,
    val category: String,
    val rounds: Int,
    val workSeconds: Int,
    val restSeconds: Int
) {
    val totalSeconds: Int
        get() = rounds * workSeconds + (rounds - 1).coerceAtLeast(0) * restSeconds
}

val workoutPresets = listOf(
    Workout(1, "Classic Boxing", "Boxing", 10, 180, 60),
    Workout(2, "Amateur Boxing", "Boxing", 3, 120, 60),
    Workout(3, "MMA", "MMA", 5, 300, 60),
    Workout(4, "Beginner Boxing", "Boxing", 3, 120, 90),
    Workout(5, "Speed Boxing", "Boxing", 8, 60, 30),
    Workout(6, "Heavy Bag", "Bag Work", 6, 180, 60),
    Workout(7, "Shadow Boxing", "Boxing", 5, 120, 30),
    Workout(8, "Kickboxing", "Kickboxing", 5, 180, 60),
    Workout(9, "Endurance Boxing", "Boxing", 12, 180, 30),
    Workout(10, "Custom Training", "Custom", 5, 180, 60)
)

fun formatDuration(seconds: Int): String =
    "${(seconds / 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}"
