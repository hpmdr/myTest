// data/local/AppDatabase.kt
package cn.debubu.mytest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import cn.debubu.mytest.data.model.PostModel

@Database(entities = [PostModel::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
}