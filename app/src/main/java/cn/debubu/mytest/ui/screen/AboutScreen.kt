package cn.debubu.mytest.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cn.debubu.mytest.ui.navigation.currentNavController

/**
 * 关于页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(name: String) {
    val navController = currentNavController()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于") },
                modifier = Modifier,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text("这是关于页面的内容,接收到的参数为：${name}")
        }
    }
}