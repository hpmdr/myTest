package cn.debubu.mytest.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import cn.debubu.mytest.ui.theme.MyTestTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val pages = listOf<@Composable () -> Unit>(
        { CellularPage() },
        { WifiPage() }
    )
    var contentPageIndex by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(initialPage = contentPageIndex, pageCount = { pages.size })

    LaunchedEffect(contentPageIndex) {
        pagerState.animateScrollToPage(contentPageIndex)
    }
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != contentPageIndex) {
            contentPageIndex = pagerState.currentPage
        }
    }

    ModalNavigationDrawer(drawerState = drawerState, gesturesEnabled = false, drawerContent = {
        ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.6f)) {
            Text("菜单")
            NavigationDrawerItem(
                label = { Text("设置") },
                selected = false,
                onClick = { scope.launch { drawerState.close() } })
            NavigationDrawerItem(
                label = { Text("关于") },
                selected = false,
                onClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate("about")
                })
        }
    }) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("首页") },
                    modifier = Modifier,
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "菜单"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "搜索"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar() {
                    NavigationBarItem(
                        selected = contentPageIndex == 0,
                        onClick = { contentPageIndex = 0 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CellTower,
                                contentDescription = "蜂窝网络信息"
                            )
                        },
                        label = { Text("蜂窝网络信息") }
                    )
                    NavigationBarItem(
                        selected = contentPageIndex == 1,
                        onClick = { contentPageIndex = 1 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "WiFi网络信息"
                            )
                        },
                        label = { Text("WiFi网络信息") }
                    )
                }
            }
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { pageIndex -> pages[pageIndex]() }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MyTestTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            //MainScreen()
        }
    }
}
