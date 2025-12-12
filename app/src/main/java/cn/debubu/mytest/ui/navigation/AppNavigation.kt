package cn.debubu.mytest.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import cn.debubu.mytest.ui.navigation.appNavigationGraph

/**
 * 路由常量定义
 */
object Screen {
    const val MAIN = "main"
    const val ABOUT = "about?name={name}"
    const val TEST_LIST = "testList"
}

/**
 * 主导航组件 - 使用官方推荐的架构模式
 * 
 * 特点：
 * 1. 不传递NavController到Screen，避免参数传递繁琐
 * 2. 使用CompositionLocal提供NavController
 * 3. 通过扩展函数管理导航操作
 * 4. 清晰的职责分离
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavigationProvider(navController) {
        NavHost(
            navController = navController,
            startDestination = Screen.MAIN
        ) {
            appNavigationGraph()
        }
    }
}