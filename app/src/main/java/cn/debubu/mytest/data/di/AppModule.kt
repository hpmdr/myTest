// data/di/AppModule.kt
package cn.debubu.mytest.data.di

import android.content.Context
import android.telephony.TelephonyManager
import androidx.room.Room
import cn.debubu.mytest.data.local.AppDatabase
import cn.debubu.mytest.data.local.PostDao
import cn.debubu.mytest.data.remote.ApiService
import cn.debubu.mytest.data.repository.CellularRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Dispatcher
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // 告诉 Hilt 这个模块的生命周期与 Application 一致
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(15, TimeUnit.SECONDS)//连接超时时间
            .readTimeout(120, TimeUnit.SECONDS)//通行超时时间
            .writeTimeout(120, TimeUnit.SECONDS)//上传超时时间
            .retryOnConnectionFailure(true)//失败重试
            .cache(Cache(directory = context.cacheDir, maxSize = 50L * 1024 * 1024))
            .dispatcher(Dispatcher().apply {
                maxRequests = 64  // 最大并发请求数（默认64）
                maxRequestsPerHost = 5  // 同一主机的最大并发请求数（默认5）
            })
            .build()
    }

    // 告诉 Hilt 如何提供一个 Retrofit 实例
    // RetrofitClient.kt 中的代码可以移到这里
    @Provides
    @Singleton // 表示这个实例在整个应用中是单例的
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        // ... 这里放入你原来 RetrofitClient.kt 中创建 Retrofit 的逻辑
        // 比如使用 kotlinx.serialization converter
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    // 告诉 Hilt 如何提供一个 ApiService 实例
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    // 告诉 Hilt 如何提供一个 AppDatabase 实例
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mytest_database"
        ).build()
    }

    // 告诉 Hilt 如何提供一个 PostDao 实例
    @Provides
    @Singleton
    fun providePostDao(database: AppDatabase): PostDao {
        return database.postDao()
    }

    @Provides
    @Singleton
    fun provideTelephonyManager(@ApplicationContext context: Context): TelephonyManager {
        return context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    @Provides
    @Singleton
    fun provideCellularRepository(telephonyManager: TelephonyManager): CellularRepository {
        return CellularRepository(telephonyManager)
    }
}
    