plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // 应用 Kotlin 序列化编译器插件
    alias(libs.plugins.kotlin.serialization)
    id("com.google.dagger.hilt.android") // 添加这一行
    kotlin("kapt") // 添加这一行，用于注解处理
}

android {
    namespace = "cn.debubu.mytest"
    compileSdk = 36

    defaultConfig {
        applicationId = "cn.debubu.mytest"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Retrofit 核心库
    implementation(libs.retrofit.core)
    // OkHttp 核心库 (可选，Retrofit 会自动拉取)
    implementation(libs.okhttp.core)
    // OkHttp 日志拦截器 (强烈推荐用于调试)
    implementation(libs.okhttp.logging)
    // 导入 kotlinx.serialization 运行时库
    implementation(libs.kotlinx.serialization.json)
    // 导入 Retrofit 的 Kotlinx 序列化转换器
    implementation(libs.retrofit.converter.serialization)

    //依赖注入
    implementation(libs.hilt.android)
    // Hilt 编译器 (注意：这里必须使用 kapt 或 ksp)
    kapt(libs.hilt.compiler)
    // Hilt Navigation Compose
    implementation(libs.hilt.navigation.compose)

    // Room 依赖 (版本号已在 libs.versions.toml 中定义)
    implementation(libs.room.runtime)
    kapt(libs.room.compiler) // 如果使用 kapt
    // ksp(libs.room.compiler) // 如果使用 ksp
    implementation(libs.room.ktx)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}