package com.example.timerboxing

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun BoxingApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "workouts") {
        composable("workouts") {
            WorkoutListScreen(onWorkoutClick = { workoutId ->
                navController.navigate("workout/$workoutId") { launchSingleTop = true }
            })
        }
        composable(
            route = "workout/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.IntType })
        ) { entry ->
            val workoutId = entry.arguments?.getInt("workoutId")
            WorkoutDetailScreen(
                workout = workoutPresets.find { it.id == workoutId },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
