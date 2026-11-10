package com.example.timerboxing

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.timerboxing.presentation.workoutdetail.WorkoutDetailRoute
import com.example.timerboxing.presentation.exerciselist.ExerciseListRoute
import com.example.timerboxing.presentation.workoutlist.WorkoutListRoute
import com.example.timerboxing.presentation.workoutsettings.WorkoutSettingsRoute

@Composable
fun BoxingApp() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as BoxingApplication
    val repository = application.repository
    NavHost(navController = navController, startDestination = "workouts") {
        composable("workouts") {
            WorkoutListRoute(repository, onWorkoutClick = { workoutId ->
                navController.navigate("workout/$workoutId") { launchSingleTop = true }
            }, onBrowseExercises = {
                navController.navigate("exercises") { launchSingleTop = true }
            })
        }
        composable("exercises") {
            ExerciseListRoute(application.exerciseRepository, onBack = { navController.popBackStack() })
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
