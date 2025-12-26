# MyTest - Android Jetpack Compose 项目说明文档

## 📋 项目概述

这是一个基于 **Jetpack Compose** 构建的现代化 Android 应用项目，采用 **MVVM 架构模式**，集成了 Android 最新的主流技术栈。项目目前处于基础框架搭建阶段，业务功能尚未完全实现，适合团队快速上手开发。

**包名**: `cn.debubu.mytest`  
**最低支持版本**: Android 7.0 (API 24)  
**目标版本**: Android 14 (API 36)  
**编译版本**: API 36

---

## 🏗️ 项目架构

### 整体架构模式：MVVM (Model-View-ViewModel)

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer (View)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │   Screen     │  │  Navigation  │  │    Theme     │   │
│  │  (Compose)   │  │   (NavHost)  │  │  (Material3) │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
└─────────────────────────────────────────────────────────┘
                          ↕ (State & Event)
┌─────────────────────────────────────────────────────────┐
│                   ViewModel Layer                       │
│  ┌──────────────────────────────────────────────────┐   │
│  │   ViewModel (Hilt Injected)                      │   │
│  │   - State Management                             │   │
│  │   - Business Logic                               │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                          ↕ (Data Flow)
┌─────────────────────────────────────────────────────────┐
│                   Repository Layer                      │
│  ┌─────────────────┐         ┌─────────────────┐        │
│  │  Repository     │ ←─────→ │  Repository     │        │
│  │  (Data Source   │         │  (单一数据源)     │        │
│  │   Coordination) │         │                 │        │
│  └─────────────────┘         └─────────────────┘        │
└─────────────────────────────────────────────────────────┘
            ↕                              ↕
┌──────────────────────┐      ┌──────────────────────┐
│   Data Layer (Local) │      │  Data Layer (Remote) │
│  ┌────────────────┐  │      │  ┌────────────────┐  │
│  │  Room Database │  │      │  │  Retrofit API  │  │
│  │  + DAO         │  │      │  │  + OkHttp      │  │
│  └────────────────┘  │      │  └────────────────┘  │
└──────────────────────┘      └──────────────────────┘
```

### 项目目录结构

```
app/src/main/java/cn/debubu/mytest/
│
├── data/                           # 数据层
│   ├── di/                         # 依赖注入模块
│   │   └── AppModule.kt            # Hilt DI 配置 (提供单例对象)
│   │
│   ├── local/                      # 本地数据源
│   │   ├── AppDatabase.kt          # Room 数据库定义
│   │   └── PostDao.kt              # 数据访问对象
│   │
│   ├── remote/                     # 远程数据源
│   │   ├── ApiService.kt           # Retrofit API 接口定义
│   │   └── RetrofitClient.kt       # Retrofit 客户端配置 (已迁移到 DI)
│   │
│   ├── model/                      # 数据模型
│   │   └── PostModel.kt            # 实体类/数据类
│   │
│   └── repository/                 # 仓库层 (协调数据源)
│       ├── PostRepository.kt       # 文章数据仓库
│       └── CellularRepository.kt   # 蜂窝网络数据仓库
│
├── ui/                             # UI 层
│   ├── navigation/                 # 导航管理
│   │   ├── AppNavigation.kt        # 导航主入口 + 路由常量
│   │   ├── NavigationExtensions.kt # 导航扩展函数
│   │   ├── LocalNavControllerProvider.kt  # NavController 依赖注入
│   │   └── NavigationUsageExample.kt      # 导航使用示例
│   │
│   ├── screen/                     # 页面组件 (Composable)
│   │   ├── MainScreen.kt           # 主页面
│   │   ├── AboutScreen.kt          # 关于页面
│   │   ├── TestListScreen.kt       # 测试列表页面
│   │   ├── CellularPage.kt         # 蜂窝网络页面
│   │   └── WifiPage.kt             # WiFi 页面
│   │
│   ├── theme/                      # 主题配置
│   │   ├── Color.kt                # 颜色定义
│   │   ├── Theme.kt                # 主题配置
│   │   └── Type.kt                 # 字体样式
│   │
│   └── viewmodel/                  # ViewModel 层
│       ├── PostViewModel.kt        # 文章相关的 VM
│       └── CellularViewModel.kt    # 蜂窝网络相关的 VM
│
├── MainActivity.kt                 # 主 Activity 入口
└── MyApplication.kt                # Application 入口 (Hilt 初始化)
```

---

## 🛠️ 技术栈详解

### 1. 核心框架

| 技术 | 版本 | 用途 | 说明 |
|-----|------|-----|------|
| **Kotlin** | 2.2.21 | 开发语言 | 官方推荐的 Android 开发语言 |
| **Android Gradle Plugin** | 8.13.1 | 构建工具 | 项目编译和打包 |
| **Jetpack Compose** | 2025.12.00 (BOM) | 声明式 UI 框架 | 替代传统 XML 布局 |
| **Material 3** | - | UI 设计系统 | Google 最新设计语言 |

### 2. 架构组件

#### 2.1 依赖注入
- **Hilt** (v2.57.2)
  - 基于 Dagger 的依赖注入框架
  - 配置文件：`AppModule.kt`
  - 提供单例对象：Retrofit、OkHttp、Room、TelephonyManager
  - 注解使用：
    - `@HiltAndroidApp` - Application 类
    - `@AndroidEntryPoint` - Activity/Fragment/ViewModel
    - `@Inject` - 构造函数注入

#### 2.2 数据持久化
- **Room** (v2.8.4)
  - SQLite 封装库，提供编译时 SQL 验证
  - 数据库：`AppDatabase.kt`
  - DAO：`PostDao.kt`
  - Entity：`PostModel.kt`
  - 支持 Kotlin Coroutines

#### 2.3 网络请求
- **Retrofit** (v3.0.0)
  - RESTful API 客户端
  - 配置：使用 Kotlinx Serialization 转换器
  - 基础 URL：`https://jsonplaceholder.typicode.com/`
  
- **OkHttp** (v5.3.2)
  - HTTP 客户端
  - 配置：
    - 连接超时：15 秒
    - 读取/写入超时：120 秒
    - 最大并发请求：64
    - 缓存大小：50MB
    - 日志拦截器：打印请求/响应日志

#### 2.4 序列化
- **Kotlinx Serialization** (v1.9.0)
  - Kotlin 官方序列化库
  - 替代 Gson/Moshi
  - 配置：`ignoreUnknownKeys = true` (忽略未知字段)

#### 2.5 图片加载
- **Coil** (v2.7.0)
  - Kotlin-first 图片加载库
  - 专为 Compose 优化
  - 支持异步加载、缓存、转换

#### 2.6 导航
- **Navigation Compose** (v2.9.6)
  - Jetpack Navigation 的 Compose 版本
  - 架构特点：
    - 使用 `CompositionLocal` 提供 NavController
    - 通过扩展函数管理导航操作
    - 避免 NavController 层层传递
  - 路由定义：`Screen` 对象

### 3. 生命周期与协程

| 依赖 | 版本 | 说明 |
|-----|------|------|
| Lifecycle Runtime KTX | 2.10.0 | ViewModel、LiveData 等生命周期组件 |
| Activity Compose | 1.12.1 | Activity 与 Compose 集成 |
| Core KTX | 1.17.0 | Kotlin 扩展函数 |

### 4. 测试框架

| 依赖 | 用途 |
|-----|------|
| JUnit 4 | 单元测试 |
| AndroidX Test | Android Instrumentation 测试 |
| Espresso | UI 测试 |
| Compose UI Test | Compose UI 测试 |

---

## ⚙️ 项目配置说明

### Gradle 配置

#### 版本管理方式
使用 **TOML 统一管理版本** (`gradle/libs.versions.toml`)：
- 所有依赖版本号集中在 `[versions]` 块
- 依赖坐标定义在 `[libraries]` 块
- 插件定义在 `[plugins]` 块
- 引用方式：`libs.retrofit.core`、`libs.plugins.hilt`

#### 关键配置 (`gradle.properties`)

```properties
# JVM 内存设置
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8

# 启用 AndroidX
android.useAndroidX=true

# Kotlin 代码风格
kotlin.code.style=official

# 非传递 R 类 (优化资源引用)
android.nonTransitiveRClass=true
```

#### 编译配置 (`app/build.gradle.kts`)

```kotlin
android {
    compileSdk = 36
    
    defaultConfig {
        minSdk = 24
        targetSdk = 36
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
}
```

### 权限配置 (`AndroidManifest.xml`)

```xml
<!-- 网络访问 -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- 定位权限 (用于读取基站信号) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- 电话状态权限 -->
<uses-permission android:name="android.permission.READ_BASIC_PHONE_STATE" />
<uses-permission 
    android:name="android.permission.READ_PHONE_STATE"
    android:maxSdkVersion="33" />
```

---

## 🚀 快速上手指南

### 1. 环境准备

- **Android Studio**: Ladybug 2024.2.1 或更高版本
- **JDK**: JDK 11 或更高版本
- **Gradle**: 8.x (项目自带)
- **Android SDK**: API 24-36

### 2. 项目导入

```bash
# 克隆项目 (或从现有目录打开)
cd myTest

# 使用 Android Studio 打开项目
# File -> Open -> 选择项目根目录
```

### 3. 同步依赖

```bash
# Android Studio 会自动提示同步，或手动执行
./gradlew build
```

### 4. 运行项目

- 连接 Android 设备或启动模拟器
- 点击 `Run` 按钮 (Shift + F10)
- 应用会启动到主页面 (`MainScreen.kt`)

---

## 📝 开发规范

### 1. 添加新页面

#### Step 1: 创建 Screen 组件

在 `ui/screen/` 下创建新的 Composable：

```kotlin
// ui/screen/NewScreen.kt
@Composable
fun NewScreen() {
    // UI 实现
}
```

#### Step 2: 添加路由常量

在 `AppNavigation.kt` 的 `Screen` 对象中添加：

```kotlin
object Screen {
    const val NEW_SCREEN = "newScreen"
    // ...其他路由
}
```

#### Step 3: 注册路由

在导航图中添加路由 (通常在单独的 `NavigationGraph.kt` 文件中)：

```kotlin
composable(Screen.NEW_SCREEN) {
    NewScreen()
}
```

#### Step 4: 导航跳转

使用扩展函数进行导航：

```kotlin
// 在任何 Composable 中
val navController = LocalNavController.current
navController.navigate(Screen.NEW_SCREEN)
```

### 2. 添加网络请求

#### Step 1: 定义 API 接口

在 `ApiService.kt` 中添加：

```kotlin
@GET("users")
suspend fun getUsers(): List<User>
```

#### Step 2: 创建数据模型

在 `data/model/` 下创建：

```kotlin
@Serializable
data class User(
    val id: Int,
    val name: String
)
```

#### Step 3: 创建 Repository

在 `data/repository/` 下创建：

```kotlin
class UserRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getUsers(): Result<List<User>> {
        return try {
            Result.success(apiService.getUsers())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### Step 4: 在 ViewModel 中调用

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    
    private val _users = mutableStateOf<List<User>>(emptyList())
    val users: State<List<User>> = _users
    
    init {
        viewModelScope.launch {
            repository.getUsers().onSuccess {
                _users.value = it
            }
        }
    }
}
```

### 3. 添加本地数据库表

#### Step 1: 创建 Entity

```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String
)
```

#### Step 2: 创建 DAO

```kotlin
@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
}
```

#### Step 3: 更新 Database

在 `AppDatabase.kt` 中：

```kotlin
@Database(
    entities = [PostModel::class, UserEntity::class], 
    version = 2  // 版本号加1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun userDao(): UserDao
}
```

#### Step 4: 在 AppModule 中提供 DAO

```kotlin
@Provides
@Singleton
fun provideUserDao(database: AppDatabase): UserDao {
    return database.userDao()
}
```

### 4. 依赖注入最佳实践

#### ViewModel 注入

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel()
```

#### Repository 注入

```kotlin
class MyRepository @Inject constructor(
    private val apiService: ApiService,
    private val dao: MyDao
)
```

#### 在 Composable 中获取 ViewModel

```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel()
) {
    // 使用 viewModel
}
```

---

## 🔍 关键技术点说明

### 1. Compose 状态管理

项目使用 **单向数据流 (UDF)** 模式：

```kotlin
// ViewModel
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {
    
    // 私有可变状态
    private val _uiState = mutableStateOf(PostUiState())
    // 公开不可变状态
    val uiState: State<PostUiState> = _uiState
    
    // 事件处理
    fun onEvent(event: PostEvent) {
        when (event) {
            is PostEvent.LoadPosts -> loadPosts()
            is PostEvent.DeletePost -> deletePost(event.id)
        }
    }
}

// Screen 中观察状态
@Composable
fun PostScreen(viewModel: PostViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState
    
    LazyColumn {
        items(uiState.posts) { post ->
            PostItem(
                post = post,
                onDelete = { viewModel.onEvent(PostEvent.DeletePost(post.id)) }
            )
        }
    }
}
```

### 2. 协程与异步处理

所有网络请求和数据库操作都使用 Kotlin Coroutines：

```kotlin
// Repository 层
suspend fun fetchPosts(): Result<List<Post>> = withContext(Dispatchers.IO) {
    try {
        val posts = apiService.getPosts()
        dao.insertAll(posts)
        Result.success(posts)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// ViewModel 层
fun loadPosts() {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)
        repository.fetchPosts()
            .onSuccess { posts ->
                _uiState.value = _uiState.value.copy(
                    posts = posts,
                    isLoading = false
                )
            }
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    error = error.message,
                    isLoading = false
                )
            }
    }
}
```

### 3. Navigation Compose 架构

项目使用官方推荐的导航架构，避免 NavController 层层传递：

```kotlin
// 1. 通过 CompositionLocal 提供 NavController
val LocalNavController = staticCompositionLocalOf<NavController> {
    error("No NavController provided")
}

@Composable
fun NavigationProvider(
    navController: NavController,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalNavController provides navController) {
        content()
    }
}

// 2. 在任何 Composable 中使用
@Composable
fun MyScreen() {
    val navController = LocalNavController.current
    Button(onClick = { navController.navigate("detail") }) {
        Text("Go to Detail")
    }
}

// 3. 或者使用扩展函数
fun NavController.navigateToDetail(id: Int) {
    navigate("detail/$id")
}
```

### 4. 数据缓存策略

Repository 层实现了本地优先的缓存策略：

```kotlin
class PostRepository @Inject constructor(
    private val apiService: ApiService,
    private val postDao: PostDao
) {
    suspend fun getPosts(forceRefresh: Boolean = false): List<Post> {
        // 优先返回本地缓存
        if (!forceRefresh) {
            val cachedPosts = postDao.getAllPosts()
            if (cachedPosts.isNotEmpty()) {
                return cachedPosts
            }
        }
        
        // 从网络获取并缓存
        val remotePosts = apiService.getPosts()
        postDao.insertAll(remotePosts)
        return remotePosts
    }
}
```

---

## 📦 项目依赖图

```
MyApplication (Hilt)
    └── MainActivity (AndroidEntryPoint)
            └── AppNavigation
                    ├── MainScreen
                    │   └── PostViewModel (HiltViewModel)
                    │       └── PostRepository (Inject)
                    │           ├── ApiService (Singleton)
                    │           │   └── Retrofit (Singleton)
                    │           │       └── OkHttpClient (Singleton)
                    │           └── PostDao (Singleton)
                    │               └── AppDatabase (Singleton)
                    │
                    ├── CellularPage
                    │   └── CellularViewModel (HiltViewModel)
                    │       └── CellularRepository (Inject)
                    │           └── TelephonyManager (Singleton)
                    │
                    └── AboutScreen
```

---

## 🐛 常见问题

### 1. Hilt 编译错误

**问题**: `@HiltAndroidApp` 注解报错  
**解决**: 确保在 `MyApplication.kt` 中使用了注解，并在 `AndroidManifest.xml` 中声明：

```xml
<application
    android:name=".MyApplication"
    ...>
```

### 2. Room 数据库版本冲突

**问题**: 修改 Entity 后应用崩溃  
**解决**: 
- 增加 `@Database` 的 `version` 号
- 提供迁移策略或使用 `.fallbackToDestructiveMigration()`

### 3. 网络请求失败

**问题**: `UnknownHostException` 或 `SocketTimeoutException`  
**解决**:
- 检查 `AndroidManifest.xml` 中是否添加了 `INTERNET` 权限
- 检查设备网络连接
- 检查 API 地址是否正确

### 4. NavController 找不到

**问题**: `No NavController provided` 错误  
**解决**: 确保 Composable 在 `NavigationProvider` 作用域内

---

## 📚 学习资源

### 官方文档

- [Jetpack Compose 官方指南](https://developer.android.com/jetpack/compose)
- [Hilt 依赖注入](https://developer.android.com/training/dependency-injection/hilt-android)
- [Room 数据库](https://developer.android.com/training/data-storage/room)
- [Retrofit 网络请求](https://square.github.io/retrofit/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

### 项目相关技术博客

- [Compose 状态管理最佳实践](https://developer.android.com/jetpack/compose/state)
- [MVVM 架构指南](https://developer.android.com/jetpack/guide)
- [Navigation Compose 深入解析](https://developer.android.com/jetpack/compose/navigation)

---

## 📄 版本历史

| 版本 | 日期 | 说明 |
|-----|------|------|
| 1.0 | 2025-12-15 | 初始版本，完成基础架构搭建 |

---

## 👥 贡献指南

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

---

## 📝 代码规范

- **命名规范**: 遵循 Kotlin 官方命名规范
- **注释**: 复杂业务逻辑必须添加注释
- **格式化**: 使用 Android Studio 默认格式化配置
- **Commit 规范**: 使用语义化提交消息 (feat/fix/docs/refactor)

---

## 📞 联系方式

如有问题或建议，请联系项目维护者或提交 Issue。

---

**Happy Coding! 🎉**
