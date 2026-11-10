import java.util.Properties
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

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
        versionCode = 9
        versionName = "1.1.7"
        buildConfigField("String", "COMMUNITY_PROJECT", "\"${community.getProperty("projectId", "")}\"")
        buildConfigField("String", "COMMUNITY_KEY", "\"${community.getProperty("apiKey", "")}\"")
        // Optional: the id of a blog post or page whose comments are the community's general board (without Firebase).
        // Support: PayPal and UPI buttons (the same details as the blog's support box). Off by default because Google Play
        // expects tips to the developer to go through Play Billing; preview APKs are built with -PsupportLinks=true.
        // Preview APKs can be limited to phone processors (-Pabis=arm64-v8a,armeabi-v7a); Play's .aab splits by itself.
        (project.findProperty("abis") as String?)?.let { ndk { abiFilters += it.split(",") } }
        val links = (project.findProperty("supportLinks") ?: "false") == "true"
        buildConfigField("boolean", "SUPPORT_LINKS", "$links")
        // Without the switch the payment details aren't even in the app.
        buildConfigField("String", "SUPPORT_PAYPAL", if (links) "\"VenkateshramuM\"" else "\"\"")
        buildConfigField("String", "SUPPORT_UPI", if (links) "\"venkateshramu000@okhdfcbank\"" else "\"\"")
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

    // The translation engine lives in an on-demand module so the app itself stays small.
    dynamicFeatures += setOf(":translate")

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
    // Play Feature Delivery: the translation engine (module :translate, ML Kit) is downloaded only when first used.
    implementation("com.google.android.play:feature-delivery-ktx:2.1.0")
    // "Support Plainly" tips through Google Play.
    implementation("com.android.billingclient:billing-ktx:8.0.0")

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
    // Each test class gets a fresh JVM, so app-wide objects (billing, WorkManager, static state) can't leak between classes.
    forkEvery = 1
    systemProperty("robolectric.dependency.repo.url", "https://maven-central.storage-download.googleapis.com/maven2")
    systemProperty("roborazzi.test.record", "true")
    systemProperty("roborazzi.record.filePathStrategy", "relativePathFromCurrentDirectory")
    maxHeapSize = "3g"
    testLogging { events("passed", "failed"); exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL; showStandardStreams = true }
}

// The .aab is what goes to Google Play, so it must never carry the sample ad IDs.
tasks.matching { it.name == "bundleRelease" }.configureEach {
    doFirst {
        // -Ppreview=true builds a bundle only to make a test APK (bundletool universal); it is never uploaded.
        if (project.findProperty("preview") != "true" && (admob.getProperty("appId") == null || admob.getProperty("banner") == null || admob.getProperty("interstitial") == null))
            throw GradleException("admob.properties with appId, banner and interstitial is required for the Play Store bundle.")
    }
}

// A Google Play upload must never carry outside payment links (Play Payments policy) or sample ads.
gradle.taskGraph.whenReady {
    if (allTasks.any { it.name == "bundleRelease" } && (project.findProperty("supportLinks") ?: "false") == "true" && project.findProperty("preview") != "true")
        throw GradleException("Don't build the Play bundle with -PsupportLinks=true: Google Play requires tips to go through Play Billing.")
}

// Every component the base app declares must have its class in the base app. A class that only ships in an on-demand
// module (the translator) can't be created on a Google Play install before that module is downloaded, and Android stops
// the app at launch (this happened in 1.1.0–1.1.2 with ML Kit's start-up provider). Activities, services and receivers
// tagged with android:splitName are only created after their module is installed, so they're allowed; providers never are.
tasks.matching { it.name == "bundleRelease" }.configureEach {
    doLast {
        val manifest = layout.buildDirectory.file("intermediates/merged_manifest/release/processReleaseMainManifest/AndroidManifest.xml").get().asFile
        val aab = layout.buildDirectory.file("outputs/bundle/release/app-release.aab").get().asFile
        val dex = ZipFile(aab).use { z ->
            z.entries().asSequence().filter { it.name.startsWith("base/dex/") && it.name.endsWith(".dex") }
                .map { e -> z.getInputStream(e).use { it.readBytes() }.toString(Charsets.ISO_8859_1) }.toList()
        }
        val ns = "http://schemas.android.com/apk/res/android"
        val doc = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }.newDocumentBuilder().parse(manifest)
        val problems = listOf("activity", "service", "receiver", "provider").flatMap { tag ->
            val nodes = doc.getElementsByTagName(tag)
            (0 until nodes.length).mapNotNull { i ->
                val el = nodes.item(i) as org.w3c.dom.Element
                val name = el.getAttributeNS(ns, "name")
                val split = el.getAttributeNS(ns, "splitName")
                val inBase = dex.any { it.contains("L" + name.replace('.', '/') + ";") }
                when {
                    inBase -> null
                    split.isNotEmpty() && tag != "provider" -> null
                    else -> "$tag $name" + if (split.isNotEmpty()) " (from on-demand module '$split')" else ""
                }
            }
        }
        if (problems.isNotEmpty()) throw GradleException(
            "The base app declares components whose classes aren't in the base app. A Google Play install would stop at launch:\n  " +
                problems.joinToString("\n  ") + "\nRemove them from the merged manifest (tools:node=\"remove\") or move the code into the base app.")
        logger.lifecycle("Base manifest check: every component the base app declares is in the base app.")
    }
}
