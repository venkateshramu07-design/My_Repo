plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.paparazzi)
}

// Firebase is optional at build time: drop app/google-services.json in to go live.
// Without it the app runs in Demo mode with sample data, so it is always runnable.
val hasFirebaseConfig = file("google-services.json").exists()
if (hasFirebaseConfig) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
}

val inviteHost: String = (project.findProperty("batchmates.inviteHost") as String?) ?: "batchmates-app.web.app"

android {
    namespace = "com.batchmates.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.batchmates.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 5
        versionName = "1.2.0"
        manifestPlaceholders["inviteHost"] = inviteHost
        buildConfigField("String", "INVITE_HOST", "\"$inviteHost\"")
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        // Optional release signing from environment (CI secrets). Falls back to debug signing.
        create("release") {
            val ks = System.getenv("BM_KEYSTORE_PATH")
            if (ks != null && file(ks).exists()) {
                storeFile = file(ks)
                storePassword = System.getenv("BM_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("BM_KEY_ALIAS")
                keyPassword = System.getenv("BM_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (System.getenv("BM_KEYSTORE_PATH") != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.all { it.systemProperty("live", System.getProperty("live") ?: "false") }
    }
    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.splashscreen)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.animation)
    debugImplementation(libs.compose.ui.tooling)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.credentials)
    implementation(libs.credentials.play)
    implementation(libs.googleid)

    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play)
    implementation(libs.osmdroid)
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
