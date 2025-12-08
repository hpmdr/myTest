package cn.debubu.mytest.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * WiFi网络详细信息页面
 */
@Composable
fun WifiPage() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("这是显示WiFi信息的页面")
    }
}