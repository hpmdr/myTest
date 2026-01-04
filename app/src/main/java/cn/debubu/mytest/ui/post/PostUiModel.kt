package cn.debubu.mytest.ui.post

/**
 * Post UI 数据模型（UiModel）
 * 
 * 作用：
 * - 用于 UI 层显示，包含格式化后的数据和计算字段
 * - 专门为 UI 设计，提供用户友好的数据格式
 * - 可以包含 UI 特有的计算字段和状态
 * 
 * 数据流向：
 * - PostEntity（数据库实体）→ PostUiModelMapper.toUiModel() → PostUiModel
 * - PostUiModel → 封装到 PostUiState → UI 层显示
 * 
 * 位置：ui/model/
 * 
 * 为什么需要单独的 UiModel：
 * 1. 数据格式化：将数据库的原始数据转换为用户友好的格式
 *    - 例如：时间戳 → "2024-01-01 12:00:00" 或 "今天 12:00"
 * 2. 计算字段：添加 UI 特有的计算字段
 *    - 例如：authorName = "User ${userId}"
 * 3. 状态字段：添加 UI 特有的状态标记
 *    - 例如：isRecent = 判断是否为最近的帖子
 * 4. 解耦：UI 需求变化不会影响数据层
 * 
 * 与 PostEntity 的区别：
 * - PostEntity：存储原始数据，便于数据库操作和排序
 * - PostUiModel：包含格式化数据，便于 UI 显示
 * 
 * 示例：
 * - PostEntity.userId: Int → PostUiModel.authorName: String ("User 1")
 * - PostEntity.createdAt: Long → PostUiModel.displayDate: String ("今天 12:00")
 * - PostEntity → PostUiModel.isRecent: Boolean (计算字段)
 */
data class PostUiModel(
    val id: Int,
    val title: String,
    val body: String,
    val authorName: String,
    val displayDate: String,
    val isRecent: Boolean
)
