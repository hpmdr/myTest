package cn.debubu.mytest.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import cn.debubu.mytest.data.model.PostModel
import cn.debubu.mytest.ui.viewmodel.PostUiState
import cn.debubu.mytest.ui.viewmodel.PostViewModel

/**
 * 测试网络加载列表的主屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestListScreen(navController: NavController, postViewModel: PostViewModel = hiltViewModel()) {

    // 从 ViewModel 收集 UI 状态
    val uiState by postViewModel.uiState.collectAsState()

    // 使用 Scaffold 添加 TopBar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post List") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        // 根据不同的 UI 状态显示不同的 Composable
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val state = uiState) {
                is PostUiState.Loading -> {
                    LoadingScreen()
                }

                is PostUiState.Success -> {
                    PostList(posts = state.posts)
                }

                is PostUiState.Error -> {
                    ErrorScreen(message = state.message) {
                        // 提供重试功能
                        postViewModel.fetchPosts()
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(modifier: Modifier = Modifier, message: String, onRetry: () -> Unit) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun PostList(posts: List<PostModel>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(posts) { post ->
            PostItem(post = post)
        }
    }
}

@Composable
fun PostItem(post: PostModel, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "id:${post.id}", style = MaterialTheme.typography.labelMedium)
            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text(text = post.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "post by user ${post.userId}",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = post.body,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
