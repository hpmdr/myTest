package cn.debubu.mytest.data.post

import kotlinx.serialization.Serializable

/**
 * Post 数据传输对象（DTO - Data Transfer Object）
 * 
 * 作用：
 * - 用于网络层，对接 API 返回的 JSON 数据
 * - 使用 @Serializable 注解，Retrofit 会自动将 JSON 转换为该对象
 * - 完全匹配 API 返回的数据结构
 * 
 * 数据流向：
 * - 网络请求（JSON）→ Retrofit 自动转换 → PostDto
 * - PostDto → PostDtoMapper.toEntity() → PostEntity（数据库实体）
 * 
 * 位置：data/remote/dto/
 * 
 * 为什么需要单独的 DTO：
 * 1. 解耦：API 结构变化不会影响数据库层和 UI 层
 * 2. 灵活性：可以处理 API 返回的复杂嵌套结构
 * 3. 可测试性：可以独立测试网络数据解析
 */
@Serializable
data class PostDto(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
)
