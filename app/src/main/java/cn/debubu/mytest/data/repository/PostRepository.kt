package cn.debubu.mytest.data.repository

import android.util.Log
import cn.debubu.mytest.data.local.PostDao
import cn.debubu.mytest.data.model.PostModel
import cn.debubu.mytest.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据仓库，作为统一的数据来源
 * 它负责处理数据来源（网络、数据库、缓存等）
 */
@Singleton // 让 Hilt 以单例管理它
class PostRepository @Inject constructor(
    private val apiService: ApiService,
    private val postDao: PostDao
) {

    // UI 只会调用这个方法来获取数据。它直接从数据库读取数据流。
    // 这就是单一数据源的体现。
    val allPosts: Flow<List<PostModel>> = postDao.getAllPosts()

    // 这个方法负责从网络刷新数据，并存入数据库
    suspend fun refreshPosts() {
        try {
            // 从网络获取最新数据
            val postsFromApi = apiService.getPosts()
            // 先清空数据库，确保只保留上一次的请求结果
            postDao.clearAll()
            // 将新数据存入数据库，Room 会自动通知 allPosts 的 Flow 更新
            postDao.insertAll(postsFromApi)
            Log.w("PostRepository", "发送了网络请求，替换了数据库中的数据")
        } catch (e: Exception) {
            // 处理网络错误
            throw e // 向上抛出异常，让 ViewModel 处理
        }
    }
}
