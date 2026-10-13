package com.example.timerboxing

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun BoxingApp() {
    val navController = rememberNavController()
    val repository = (LocalContext.current.applicationContext as BoxingApplication).repository
    NavHost(navController = navController, startDestination = "workouts") {
        composable("workouts") {
            WorkoutListRoute(repository, onWorkoutClick = { workoutId ->
                navController.navigate("workout/$workoutId") { launchSingleTop = true }
            })
        }
        composable(
            route = "workout/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.IntType })
        ) { entry ->
            WorkoutDetailRoute(
                repository = repository,
                onBack = { navController.popBackStack() },
                onSettings = {
                    navController.navigate("settings/${entry.arguments?.getInt("workoutId")}") { launchSingleTop = true }
                }
            )
        }
        composable(
            route = "settings/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.IntType })
        ) {
            WorkoutSettingsRoute(
                repository = repository,
                onBack = { navController.popBackStack() },
                onSaved = { workoutId ->
                    navController.popBackStack("workout/$workoutId", inclusive = false)
                }
            )
        }
    }
}
