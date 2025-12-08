package cn.debubu.mytest.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 蜂窝网络详细信息页面
 */
@Composable
fun CellularPage(){
    Column(modifier = Modifier.fillMaxSize()) {
        Text("这是显示移动网络信息的页面")
    }
}