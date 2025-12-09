package cn.debubu.mytest.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cn.debubu.mytest.ui.screen.AboutScreen
import cn.debubu.mytest.ui.screen.MainScreen

object Screen {
    const val MAIN = "main"
    const val ABOUT = "about?name={name}"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.MAIN) {
        composable(Screen.MAIN) { MainScreen(navController) }
        composable(
            Screen.ABOUT,
            arguments = listOf(
                navArgument("name") {
                    nullable = false
                    type = NavType.StringType
                    defaultValue = "默认名字"
                })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: "未知"
            AboutScreen(navController, name)
        }
    }
}