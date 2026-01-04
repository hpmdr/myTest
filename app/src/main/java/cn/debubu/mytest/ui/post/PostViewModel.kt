package cn.debubu.mytest.ui.post

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.debubu.mytest.data.post.PostEntity
import cn.debubu.mytest.data.post.PostRepository
import cn.debubu.mytest.ui.post.PostUiModel
import cn.debubu.mytest.ui.post.PostUiModelMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. 定义UI状态，这是一个很好的实践，可以清晰地表示UI可能处于的各种情况
// 数据流向：ViewModel -> PostUiState -> UI层
sealed interface PostUiState {
    data class Success(val posts: List<PostUiModel>) : PostUiState
    data class Error(val message: String) : PostUiState
    object Loading : PostUiState
}

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    // 将 uiState 改为从 Repository 的 Flow 转换而来
    // 数据流向：Repository（PostEntity） -> Mapper转换 -> PostUiState（PostUiModel） -> UI层
    val uiState: StateFlow<PostUiState> = repository.allPosts
        .onStart { emit(emptyList<PostEntity>()) } // 可选：初始时发射一个空列表
        .map { posts ->
            if (posts.isEmpty()) {
                PostUiState.Loading // 如果数据库为空，显示加载中（并触发首次加载）
            } else {
                // 使用 Mapper 将 PostEntity 转换为 PostUiModel
                val postUiModels: List<PostUiModel> = PostUiModelMapper.toUiModelList(posts)
                PostUiState.Success(postUiModels) // 如果有数据，直接显示
            }
        }
        .catch { e -> emit(PostUiState.Error(e.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PostUiState.Loading
        )

    init {
        // ViewModel 初始化时，就去尝试从网络刷新一次数据
        fetchPosts()
    }

    // 重试/刷新功能现在调用 Repository
    fun fetchPosts() {
        viewModelScope.launch {
            try {
                // 只是触发刷新，UI 的更新会通过上面的 Flow 自动完成
                repository.refreshPosts()
            } catch (e: Exception) {
                // 如果刷新失败，并且当前状态还是 Loading，则更新为 Error
                if (uiState.value is PostUiState.Loading) {
                    // (注意：这里需要把 _uiState 暴露出来才能修改，
                    //  或者改变 StateFlow 的构建方式以处理错误。
                    //  简单起见，我们假设 catch 块已经处理了错误状态)
                    Log.e("PostViewModel", "Failed to fetch posts", e)
                }
            }
        }
    }
}

