package com.rich_beluga.richnote.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rich_beluga.richnote.ui.AboutScreen
import com.rich_beluga.richnote.ui.SettingsScreen

@Composable
fun Navigation(onExitSettings: () -> Unit) {
    val navController = rememberNavController()

    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Settings,
            enterTransition = { slideFadeInFromRight() },
            exitTransition = { slideFadeOutToLeft() },
            popEnterTransition = { slideFadeInFromLeft() },
            popExitTransition = { slideFadeOutToRight() }
        ) {
            composable<NavRoutes.Settings> {
                SettingsScreen(
                    onBackClick = onExitSettings,
                    onAboutClick = { navController.navigate(NavRoutes.About) }
                )
            }

            composable<NavRoutes.About> {
                AboutScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
