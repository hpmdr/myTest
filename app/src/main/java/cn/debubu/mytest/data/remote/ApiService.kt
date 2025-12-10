package cn.debubu.mytest.data.remote

import cn.debubu.mytest.data.model.PostModel
import retrofit2.http.GET

interface ApiService {
    // GET 请求，路径为 posts。返回 Post 对象的列表。
    @GET("posts")
    suspend fun getPosts(): List<PostModel>

    // 假设我们还有一个获取单个帖子的方法
    // @GET("posts/{id}")
    // suspend fun getPostById(@Path("id") postId: Int): Post
}