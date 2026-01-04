// data/local/AppDatabase.kt
package cn.debubu.mytest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import cn.debubu.mytest.data.post.PostEntity

@Database(entities = [PostEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
}