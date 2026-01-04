package cn.debubu.mytest.data.post

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Post 数据库实体（Entity）
 * 
 * 作用：
 * - 用于本地数据库存储，使用 Room 数据库框架
 * - 使用 @Entity 注解标记为数据库表
 * - 使用 @PrimaryKey 注解标记主键
 * - Room 会自动将该对象映射到数据库表
 * 
 * 数据流向：
 * - PostDto → PostDtoMapper.toEntity() → PostEntity
 * - PostEntity → Room 自动存储 → 数据库表
 * - 数据库 → Room 自动查询 → PostEntity
 * - PostEntity → PostUiModelMapper.toUiModel() → PostUiModel（UI 模型）
 * 
 * 位置：data/local/entity/
 * 
 * 为什么需要单独的 Entity：
 * 1. 数据库优化：可以添加索引、约束等数据库特有的配置
 * 2. 存储效率：可以优化数据类型以节省存储空间
 * 3. 查询性能：可以添加索引以提高查询速度
 * 4. 解耦：数据库结构变化不会影响网络层和 UI 层
 * 
 * 注意事项：
 * - Entity 的字段应该与数据库表结构完全匹配
 * - 可以添加 @ColumnInfo 注解来指定列名
 * - 可以添加 @Index 注解来创建索引
 * - 可以添加 @Ignore 注解来忽略不需要存储的字段
 */
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
)
