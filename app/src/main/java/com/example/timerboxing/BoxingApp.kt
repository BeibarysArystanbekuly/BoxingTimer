package com.example.timerboxing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun BoxingApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val store = remember(context) { WorkoutStore(context) }
    var workouts by remember { mutableStateOf(store.load()) }
    NavHost(navController = navController, startDestination = "workouts") {
        composable("workouts") {
            WorkoutListScreen(workouts = workouts, onWorkoutClick = { workoutId ->
                navController.navigate("workout/$workoutId") { launchSingleTop = true }
            })
        }
        composable(
            route = "workout/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.IntType })
        ) { entry ->
            val workoutId = entry.arguments?.getInt("workoutId")
            WorkoutDetailScreen(
                workout = workouts.find { it.id == workoutId },
                onBack = { navController.popBackStack() },
                onSettings = {
                    navController.navigate("settings/$workoutId") { launchSingleTop = true }
                }
            )
        }
        composable(
            route = "settings/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.IntType })
        ) { entry ->
            val workoutId = entry.arguments?.getInt("workoutId")
            WorkoutSettingsScreen(
                workout = workouts.find { it.id == workoutId },
                onBack = { navController.popBackStack() },
                onSave = { updated ->
                    store.save(updated)
                    workouts = workouts.map { if (it.id == updated.id) updated else it }
                    navController.popBackStack("workout/${updated.id}", inclusive = true)
                    navController.navigate("workout/${updated.id}")
                }
            )
        }
    }
}
