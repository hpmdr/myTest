// data/model/PostModel.kt
package cn.debubu.mytest.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "posts") // 1. 标记为实体，并指定表名
data class PostModel(
    @PrimaryKey // 2. 标记为主键
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
)
    