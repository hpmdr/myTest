package cn.debubu.mytest.data.post

import cn.debubu.mytest.data.post.PostEntity
import cn.debubu.mytest.data.post.PostDto

/**
 * PostDto 到 PostEntity 的转换器（Mapper）
 * 
 * 作用：
 * - 将网络层的 PostDto 转换为数据库层的 PostEntity
 * - 这是数据流中的第一个手动转换点
 * 
 * 数据流向：
 * - 网络请求（JSON）→ Retrofit 自动转换 → PostDto
 * - PostDto → PostDtoMapper.toEntity() → PostEntity
 * - PostEntity → Room 自动存储 → 数据库
 * 
 * 位置：data/remote/mapper/
 * 
 * 为什么需要 Mapper：
 * 1. 解耦：网络层和数据库层完全解耦，互不影响
 * 2. 灵活性：可以在转换过程中添加数据验证、默认值处理等逻辑
 * 3. 可维护性：转换逻辑集中管理，易于维护和修改
 * 4. 可测试性：可以独立测试转换逻辑
 * 
 * 使用场景：
 * - 在 PostRepository.refreshPosts() 中调用
 * - 从网络获取数据后，转换为 Entity 存储到数据库
 * 
 * 示例调用：
 * ```kotlin
 * val postsFromApi = apiService.getPosts()  // List<PostDto>
 * val entities = PostDtoMapper.toEntityList(postsFromApi)  // List<PostEntity>
 * postDao.insertAll(entities)
 * ```
 * 
 * 注意事项：
 * - 使用 object 声明，表示这是一个单例对象
 * - 提供单个转换和批量转换两种方法
 * - 转换逻辑应该简单直接，避免复杂的业务逻辑
 */
object PostDtoMapper {
    
    /**
     * 将单个 PostDto 转换为 PostEntity
     * 
     * @param dto 网络层的数据传输对象
     * @return 数据库层的实体对象
     * 
     * 转换逻辑：
     * - 直接映射字段值
     * - 可以在这里添加数据验证、默认值处理等逻辑
     */
    fun toEntity(dto: PostDto): PostEntity = PostEntity(
        id = dto.id,
        userId = dto.userId,
        title = dto.title,
        body = dto.body
    )
    
    /**
     * 将 PostDto 列表转换为 PostEntity 列表
     * 
     * @param dtos 网络层的数据传输对象列表
     * @return 数据库层的实体对象列表
     * 
     * 使用场景：
     * - 批量处理网络返回的数据
     * - 一次性将多个 DTO 转换为 Entity
     */
    fun toEntityList(dtos: List<PostDto>): List<PostEntity> = 
        dtos.map { toEntity(it) }
}
