package cn.debubu.mytest.data.post

import cn.debubu.mytest.data.local.PostDao
import cn.debubu.mytest.data.post.PostEntity
import cn.debubu.mytest.data.remote.ApiService
import cn.debubu.mytest.data.post.PostDto
import cn.debubu.mytest.data.post.PostDtoMapper
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
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
    // 数据流向：数据库（PostEntity） -> Flow -> ViewModel
    val allPosts: Flow<List<PostEntity>> = postDao.getAllPosts()

    // 这个方法负责从网络刷新数据，并存入数据库
    // 数据流向：网络（PostDto） -> Mapper转换 -> 数据库（PostEntity）
    suspend fun refreshPosts() {
        try {
            // 从网络获取最新数据（返回的是 PostDto 列表）
            val postsFromApi: List<PostDto> = apiService.getPosts()
            // 先清空数据库，确保只保留上一次的请求结果
            postDao.clearAll()
            // 使用 Mapper 将 PostDto 转换为 PostEntity
            val postsToInsert: List<PostEntity> = PostDtoMapper.toEntityList(postsFromApi)
            // 将转换后的数据存入数据库，Room 会自动通知 allPosts 的 Flow 更新
            postDao.insertAll(postsToInsert)
            Timber.w("发送了网络请求，替换了数据库中的数据")
        } catch (e: Exception) {
            // 处理网络错误
            throw e // 向上抛出异常，让 ViewModel 处理
        }
    }
}
