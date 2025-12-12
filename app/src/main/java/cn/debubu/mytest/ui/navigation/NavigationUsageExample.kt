package cn.debubu.mytest.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.debubu.mytest.ui.theme.MyTestTheme

/**
 * 导航使用示例
 * 展示如何在Screen中使用新的导航方式
 */
@Composable
fun NavigationExample() {
    val navController = currentNavController()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "导航使用示例",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            Button(
                onClick = {
                    navController.navigateToAbout("张三")
                }
            ) {
                Text("跳转到关于页面")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    navController.navigateToTestList()
                }
            ) {
                Text("跳转到测试列表")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    navController.navigateToMain()
                }
            ) {
                Text("返回主页")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavigationExamplePreview() {
    MyTestTheme {
        // 注意：在Preview中无法使用NavigationProvider
        // 这个Preview仅用于UI布局展示
        // 实际使用时需要在NavigationProvider中使用
    }
}