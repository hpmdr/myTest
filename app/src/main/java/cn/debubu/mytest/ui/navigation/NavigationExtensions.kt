package cn.debubu.mytest.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import cn.debubu.mytest.ui.screen.AboutScreen
import cn.debubu.mytest.ui.screen.MainScreen
import cn.debubu.mytest.ui.screen.TestListScreen

/**
 * 导航扩展函数 - 官方推荐的最佳实践
 */

// NavController 扩展函数，用于导航操作
fun NavController.navigateToMain(navOptions: NavOptions? = null) {
    navigate(Screen.MAIN, navOptions)
}

fun NavController.navigateToAbout(name: String = "默认名字", navOptions: NavOptions? = null) {
    navigate(Screen.ABOUT.replace("{name}", name), navOptions)
}

fun NavController.navigateToTestList(navOptions: NavOptions? = null) {
    navigate(Screen.TEST_LIST, navOptions)
}

// NavGraphBuilder 扩展函数，用于定义导航图
fun NavGraphBuilder.mainScreen() {
    composable(Screen.MAIN) {
        MainScreen()
    }
}

fun NavGraphBuilder.aboutScreen() {
    composable(
        route = Screen.ABOUT,
        arguments = listOf(
            navArgument("name") {
                nullable = false
                type = NavType.StringType
                defaultValue = "默认名字"
            }
        )
    ) { backStackEntry ->
        val name = backStackEntry.arguments?.getString("name") ?: "未知"
        AboutScreen(name)
    }
}

fun NavGraphBuilder.testListScreen() {
    composable(Screen.TEST_LIST) {
        TestListScreen()
    }
}

// 组合所有屏幕的导航图
fun NavGraphBuilder.appNavigationGraph() {
    mainScreen()
    aboutScreen()
    testListScreen()
}