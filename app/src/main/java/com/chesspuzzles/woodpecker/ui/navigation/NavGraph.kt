package com.chesspuzzles.woodpecker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chesspuzzles.woodpecker.data.preferences.AppPreferences
import com.chesspuzzles.woodpecker.ui.create.CreateSuiteScreen

import com.chesspuzzles.woodpecker.ui.detail.SuiteDetailScreen
import com.chesspuzzles.woodpecker.ui.home.HomeScreen
import com.chesspuzzles.woodpecker.ui.settings.SettingsScreen
import com.chesspuzzles.woodpecker.ui.summary.CycleSummaryScreen
import com.chesspuzzles.woodpecker.ui.training.TrainingScreen

object Routes {
    const val HOME = "home"
    const val CREATE_SUITE = "create"
    const val SETTINGS = "settings"

    const val SUITE_DETAIL = "suite/{suiteId}"
    const val TRAINING = "training/{suiteId}/{cycleId}?retry={retry}"
    const val CYCLE_SUMMARY = "summary/{cycleId}"

    fun suiteDetail(suiteId: Long) = "suite/$suiteId"
    fun training(suiteId: Long, cycleId: Long, retry: Boolean = false) = "training/$suiteId/$cycleId?retry=$retry"
    fun cycleSummary(cycleId: Long) = "summary/$cycleId"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    onLanguageChanged: (String) -> Unit,
    appPreferences: AppPreferences
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onCreateSuite = { navController.navigate(Routes.CREATE_SUITE) },
                onSuiteClick = { suiteId -> navController.navigate(Routes.suiteDetail(suiteId)) },
                onStartTraining = { suiteId, cycleId ->
                    navController.navigate(Routes.training(suiteId, cycleId))
                },
                onStartRetry = { suiteId, cycleId ->
                    navController.navigate(Routes.training(suiteId, cycleId, retry = true))
                },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                appPreferences = appPreferences
            )
        }

        composable(Routes.CREATE_SUITE) {
            CreateSuiteScreen(
                onBack = { navController.popBackStack() },
                onSuiteCreated = { suiteId ->
                    navController.popBackStack()
                    navController.navigate(Routes.suiteDetail(suiteId))
                },

            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLanguageChanged = onLanguageChanged
            )
        }


        composable(
            route = Routes.SUITE_DETAIL,
            arguments = listOf(navArgument("suiteId") { type = NavType.LongType })
        ) {
            SuiteDetailScreen(
                onBack = { navController.popBackStack() },
                onContinueCycle = { suiteId, cycleId ->
                    navController.navigate(Routes.training(suiteId, cycleId))
                },
                onStartTraining = { suiteId, cycleId ->
                    navController.navigate(Routes.training(suiteId, cycleId))
                },
                onNavigateToSuite = { suiteId ->
                    navController.navigate(Routes.suiteDetail(suiteId)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(
            route = Routes.TRAINING,
            arguments = listOf(
                navArgument("suiteId") { type = NavType.LongType },
                navArgument("cycleId") { type = NavType.LongType },
                navArgument("retry") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            TrainingScreen(
                onBack = { navController.popBackStack() },
                onCycleComplete = { cycleId ->
                    navController.popBackStack()
                    navController.navigate(Routes.cycleSummary(cycleId))
                }
            )
        }

        composable(
            route = Routes.CYCLE_SUMMARY,
            arguments = listOf(navArgument("cycleId") { type = NavType.LongType })
        ) {
            CycleSummaryScreen(
                onBack = { navController.popBackStack() },
                onStartNextCycle = { suiteId, cycleId ->
                    navController.popBackStack()
                    navController.navigate(Routes.training(suiteId, cycleId))
                },
                onStartRetry = { suiteId, cycleId ->
                    navController.popBackStack()
                    navController.navigate(Routes.training(suiteId, cycleId, retry = true))
                }
            )
        }
    }
}
