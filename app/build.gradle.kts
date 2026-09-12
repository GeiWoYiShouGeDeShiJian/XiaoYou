plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}
// Dagger/Hilt 2.59.2 内置 kotlin-metadata-jvm 仅支持到 Kotlin 元数据 2.3.0，Kotlin 2.4.0 会触发
// "Provided Metadata instance has version 2.4.0" 报错（google/dagger#5180），强制升级到 2.4.0 由官方验证可修复
configurations.configureEach {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-metadata-jvm:2.4.0")
    }
}
android {
    namespace = "com.example.jizhangruanjian"
    compileSdk {
        version = release(37)
    }
    defaultConfig {
        applicationId = "com.example.jizhangruanjian"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.sqlcipher)
    implementation(libs.commons.csv)
    implementation(libs.vico.compose.m3)
    implementation(libs.androidx.biometric)
    implementation(libs.gson)
    implementation(libs.reorderable)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.room.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
