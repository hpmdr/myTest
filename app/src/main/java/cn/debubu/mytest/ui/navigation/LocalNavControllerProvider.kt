package cn.debubu.mytest.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController

/**
 * CompositionLocal 用于提供 NavController
 * 这是官方推荐的依赖注入方式
 */
val LocalNavController = compositionLocalOf<NavController> {
    error("NavController not provided")
}

/**
 * 为Composable提供NavController的包装器
 */
@Composable
fun NavigationProvider(
    navController: NavController,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalNavController provides navController,
        content = content
    )
}

/**
 * 在Composable中获取NavController的便捷函数
 */
@Composable
fun currentNavController(): NavController = LocalNavController.current