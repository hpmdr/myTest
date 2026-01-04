package cn.debubu.mytest.data.remote

import cn.debubu.mytest.data.post.PostDto
import retrofit2.http.GET

/**
 * API 服务接口
 * 
 * 作用：
 * - 定义网络请求接口
 * - 使用 Retrofit 框架进行网络请求
 * - Retrofit 会自动将 JSON 转换为 PostDto 对象
 * 
 * 数据流向：
 * - 网络请求（JSON）→ Retrofit 自动转换 → PostDto
 * - PostDto → PostDtoMapper.toEntity() → PostEntity（数据库实体）
 * 
 * 位置：data/remote/
 * 
 * 注意事项：
 * - 返回类型使用 PostDto，而不是 PostEntity 或 PostUiModel
 * - PostDto 是网络层专用的数据模型
 * - 不要在接口中直接返回其他层的数据模型
 */
interface ApiService {
    /**
     * 获取所有帖子
     * 
     * @return PostDto 列表
     * 
     * 数据流向：
     * - 发起 GET 请求到 /posts
     * - 接收 JSON 响应
     * - Retrofit 自动将 JSON 转换为 List<PostDto>
     * - 返回 PostDto 列表给调用者
     */
    @GET("posts")
    suspend fun getPosts(): List<PostDto>

    // 假设我们还有一个获取单个帖子的方法
    // @GET("posts/{id}")
    // suspend fun getPostById(@Path("id") postId: Int): PostDto
}