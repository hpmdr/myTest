package cn.debubu.mytest.ui.post

import cn.debubu.mytest.data.post.PostEntity

/**
 * PostEntity 到 PostUiModel 的转换器（Mapper）
 * 
 * 作用：
 * - 将数据库层的 PostEntity 转换为 UI 层的 PostUiModel
 * - 这是数据流中的第二个手动转换点
 * - 包含数据格式化和计算逻辑
 * 
 * 数据流向：
 * - 数据库 → Room 自动查询 → PostEntity
 * - PostEntity → PostUiModelMapper.toUiModel() → PostUiModel
 * - PostUiModel → 封装到 PostUiState → UI 层显示
 * 
 * 位置：ui/model/
 * 
 * 为什么需要 Mapper：
 * 1. 数据格式化：将数据库的原始数据转换为用户友好的格式
 *    - 例如：userId → authorName = "User ${userId}"
 * 2. 计算字段：添加 UI 特有的计算字段
 *    - 例如：isRecent = 判断是否为最近的帖子
 * 3. 解耦：UI 需求变化不会影响数据层
 * 4. 可维护性：转换逻辑集中管理，易于维护和修改
 * 
 * 使用场景：
 * - 在 PostViewModel 中调用
 * - 使用 Flow.map 操作符转换数据流
 * - 从数据库读取数据后，转换为 UiModel 供 UI 使用
 * 
 * 示例调用：
 * ```kotlin
 * val uiState: StateFlow<PostUiState> = repository.allPosts
 *     .map { entities ->
 *         val uiModels = PostUiModelMapper.toUiModelList(entities)
 *         PostUiState.Success(uiModels)
 *     }
 *     .stateIn(...)
 * ```
 * 
 * 注意事项：
 * - 使用 object 声明，表示这是一个单例对象
 * - 提供单个转换和批量转换两种方法
 * - 转换逻辑可以包含数据格式化、计算等逻辑
 * - 避免在 Mapper 中进行耗时的操作
 */
object PostUiModelMapper {
    
    /**
     * 将单个 PostEntity 转换为 PostUiModel
     * 
     * @param entity 数据库层的实体对象
     * @return UI 层的数据模型
     * 
     * 转换逻辑：
     * - id: 直接映射
     * - title: 直接映射
     * - body: 直接映射
     * - authorName: 计算字段，从 userId 生成
     * - displayDate: 计算字段，生成友好的日期显示
     * - isRecent: 计算字段，判断是否为最近的帖子
     */
    fun toUiModel(entity: PostEntity): PostUiModel = PostUiModel(
        id = entity.id,
        title = entity.title,
        body = entity.body,
        authorName = "User ${entity.userId}",
        displayDate = getDisplayDate(),
        isRecent = isRecentPost()
    )
    
    /**
     * 将 PostEntity 列表转换为 PostUiModel 列表
     * 
     * @param entities 数据库层的实体对象列表
     * @return UI 层的数据模型列表
     * 
     * 使用场景：
     * - 批量处理数据库返回的数据
     * - 一次性将多个 Entity 转换为 UiModel
     */
    fun toUiModelList(entities: List<PostEntity>): List<PostUiModel> = 
        entities.map { toUiModel(it) }
    
    /**
     * 生成友好的日期显示
     * 
     * @return 友好的日期字符串，例如："今天 12:00" 或 "2024-01-01"
     * 
     * 注意：当前示例中 PostEntity 没有时间字段，
     * 实际项目中应该从 entity 中获取时间戳
     */
    private fun getDisplayDate(): String {
        return "今天"
    }
    
    /**
     * 判断是否为最近的帖子
     * 
     * @return true 如果是最近的帖子，否则 false
     * 
     * 注意：当前示例中 PostEntity 没有时间字段，
     * 实际项目中应该从 entity 中获取时间戳进行判断
     */
    private fun isRecentPost(): Boolean {
        return true
    }
}
