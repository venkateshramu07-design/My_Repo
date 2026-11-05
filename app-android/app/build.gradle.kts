import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// AdMob IDs come from admob.properties (appId, banner, interstitial). They aren't secret: they ship inside the app.
// Without that file, Google's sample IDs are used, which show "Test Ad" banners and earn nothing.
// Debug builds always use the sample ad units, so tapping ads while testing can never break AdMob's rules.
val admob = Properties().apply {
    val f = rootProject.file("admob.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val testAppId = "ca-app-pub-3940256099942544~3347511713"
val testBanner = "ca-app-pub-3940256099942544/9214589741"
val testInterstitial = "ca-app-pub-3940256099942544/1033173712"
fun ad(key: String, fallback: String) = (admob.getProperty(key) ?: fallback).trim()

// Community (Firebase) settings from community.properties: projectId and apiKey (a web API key; not secret, it ships in
// the app and Firestore security rules protect the data). Without them, the community runs on the blog's comments.
val community = Properties().apply {
    val f = rootProject.file("community.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

// Release signing comes from keystore.properties (never committed): storeFile, storePassword, keyAlias, keyPassword.
val signing = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.randomthoughtsever.plainly"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.randomthoughtsever.plainly"
        minSdk = 29
        targetSdk = 36
        versionCode = 2
        versionName = "1.1.0"
        buildConfigField("String", "COMMUNITY_PROJECT", "\"${community.getProperty("projectId", "")}\"")
        buildConfigField("String", "COMMUNITY_KEY", "\"${community.getProperty("apiKey", "")}\"")
        // Optional: the id of a blog post or page whose comments are the community's general board (without Firebase).
        buildConfigField("String", "COMMUNITY_BOARD", "\"${community.getProperty("board", "").filter { it.isDigit() }}\"")
        manifestPlaceholders["admobAppId"] = ad("appId", testAppId)
        buildConfigField("String", "ADMOB_BANNER", "\"${ad("banner", testBanner)}\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL", "\"${ad("interstitial", testInterstitial)}\"")
    }

    signingConfigs {
        if (signing.getProperty("storeFile") != null) {
            create("release") {
                storeFile = file(signing.getProperty("storeFile"))
                storePassword = signing.getProperty("storePassword")
                keyAlias = signing.getProperty("keyAlias")
                keyPassword = signing.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "ADMOB_BANNER", "\"$testBanner\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL", "\"$testInterstitial\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
    val bom = platform("androidx.compose:compose-bom:2025.05.01")
    implementation(bom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.browser:browser:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.android.play:review-ktx:2.0.2")
    implementation("org.jsoup:jsoup:1.18.3")
    implementation("com.google.android.gms:play-services-ads:24.9.0")
    implementation("com.google.android.ump:user-messaging-platform:3.2.0")
    implementation("androidx.webkit:webkit:1.14.0")
    implementation("androidx.work:work-runtime-ktx:2.10.1")

    // JVM tests: pure maths and parsing, plus Robolectric to render real screens for checks and store screenshots.
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.16")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation(bom)
    testImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("io.github.takahirom.roborazzi:roborazzi:1.46.1")
    testImplementation("io.github.takahirom.roborazzi:roborazzi-compose:1.46.1")
    testImplementation("io.github.takahirom.roborazzi:roborazzi-junit-rule:1.46.1")
}

tasks.withType<Test>().configureEach {
    systemProperty("robolectric.dependency.repo.url", "https://maven-central.storage-download.googleapis.com/maven2")
    systemProperty("roborazzi.test.record", "true")
    systemProperty("roborazzi.record.filePathStrategy", "relativePathFromCurrentDirectory")
    maxHeapSize = "3g"
    testLogging { events("passed", "failed"); exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL; showStandardStreams = true }
}

// The .aab is what goes to Google Play, so it must never carry the sample ad IDs.
tasks.matching { it.name == "bundleRelease" }.configureEach {
    doFirst {
        if (admob.getProperty("appId") == null || admob.getProperty("banner") == null || admob.getProperty("interstitial") == null)
            throw GradleException("admob.properties with appId, banner and interstitial is required for the Play Store bundle.")
    }
}
