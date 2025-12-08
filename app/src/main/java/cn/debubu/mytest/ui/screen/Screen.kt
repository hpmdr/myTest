package cn.debubu.mytest.ui.screen

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object Screen {
    const val MAIN = "main"
    const val ABOUT = "about"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.MAIN) {
        composable(Screen.MAIN) { MainScreen(navController) }
        composable(Screen.ABOUT) { AboutScreen(navController) }
    }
}