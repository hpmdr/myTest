// data/local/PostDao.kt
package cn.debubu.mytest.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.debubu.mytest.data.post.PostEntity
import kotlinx.coroutines.flow.Flow

/**
 * Post 数据访问对象（DAO - Data Access Object）
 * 
 * 作用：
 * - 定义数据库操作接口
 * - 使用 Room 数据库框架
 * - Room 会自动生成接口的实现
 * 
 * 数据流向：
 * - PostEntity → Room 自动存储 → 数据库
 * - 数据库 → Room 自动查询 → PostEntity
 * - PostEntity → PostUiModelMapper.toUiModel() → PostUiModel（UI 模型）
 * 
 * 位置：data/local/
 * 
 * 为什么使用 DAO：
 * 1. 类型安全：编译时检查 SQL 语句
 * 2. 自动生成：Room 自动生成实现代码
 * 3. 响应式：支持 Flow，数据变化自动通知
 * 4. 简洁：不需要手写 SQL 和数据库操作代码
 * 
 * 注意事项：
 * - 所有方法都使用 PostEntity，而不是 PostDto 或 PostUiModel
 * - PostEntity 是数据库层专用的数据模型
 * - 不要在 DAO 中直接使用其他层的数据模型
 */
@Dao
interface PostDao {
    /**
     * 插入帖子列表
     * 
     * @param posts PostEntity 列表
     * 
     * 数据流向：
     * - 接收 PostEntity 列表
     * - Room 自动存储到数据库
     * - 如果主键冲突，替换旧数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostEntity>)

    /**
     * 获取所有帖子
     * 
     * @return Flow<List<PostEntity>> 帖子列表的数据流
     * 
     * 数据流向：
     * - 查询数据库中的所有帖子
     * - Room 自动将结果转换为 PostEntity 列表
     * - 返回 Flow，当数据库变化时自动发射新数据
     * 
     * 注意：
     * - 使用 Flow 实现响应式更新
     * - 数据库变化时，Flow 会自动发射新数据
     * - ViewModel 会订阅这个 Flow 并转换为 UiModel
     */
    @Query("SELECT * FROM posts")
    fun getAllPosts(): Flow<List<PostEntity>>

    /**
     * 清空帖子表
     * 
     * 数据流向：
     * - 删除数据库中的所有帖子
     * - Flow 会自动通知订阅者数据已清空
     */
    @Query("DELETE FROM posts")
    suspend fun clearAll()
}
    