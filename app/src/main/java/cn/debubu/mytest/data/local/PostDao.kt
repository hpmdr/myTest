// data/local/PostDao.kt
package cn.debubu.mytest.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.debubu.mytest.data.model.PostModel
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    // 当插入数据冲突时，替换旧数据
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostModel>)

    // 查询所有帖子，返回一个 Flow。当数据变化时，Flow 会自动发射新数据
    @Query("SELECT * FROM posts")
    fun getAllPosts(): Flow<List<PostModel>>

    // 清空表
    @Query("DELETE FROM posts")
    suspend fun clearAll()
}
    