plugins {
    id("com.android.dynamic-feature")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.randomthoughtsever.plainly.translate"
    compileSdk = 36
    defaultConfig {
        minSdk = 29
        // Same processor limit as the app for preview APKs (-Pabis=...); Play splits by itself.
        (project.findProperty("abis") as String?)?.let { ndk { abiFilters += it.split(",") } }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildTypes { release { proguardFiles("proguard-rules.pro") } }
}

dependencies {
    implementation(project(":app"))
    implementation("com.google.mlkit:translate:17.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}
