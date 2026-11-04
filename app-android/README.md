# Blog Digest Android app

A native Android shell for https://randomthoughtsever.blogspot.com/ with bottom tabs (Home, Money, Mind, Career, Tools),
file picking and saving for the blog's in-browser tools (blob/data downloads saved to Downloads via MediaStore),
pull-to-refresh, offline screen and cache, external links in Custom Tabs, ads blocked, splash screen and adaptive icon.

- Package: `com.randomthoughtsever.blogdigest`; minSdk 29, targetSdk/compileSdk 36; AGP 8.11, Kotlin 2.1, Gradle 8.14.
- Code: `app/src/main/java/com/randomthoughtsever/blogdigest/MainActivity.kt`.
- Store assets and listing text: `store/`. Publishing steps: `PLAY_STORE_GUIDE.md`.

## Build

1. Android SDK with `platforms;android-36` and `build-tools;36.0.0`; put `sdk.dir=/path/to/sdk` in `local.properties`.
2. Signing: create `keystore.properties` (never commit it) with `storeFile`, `storePassword`, `keyAlias`, `keyPassword`.
3. `./gradlew bundleRelease assembleRelease`, which writes `app/build/outputs/bundle/release/app-release.aab` (upload this to
   Play) and `app/build/outputs/apk/release/app-release.apk` (for testing on a phone).

For each update, raise `versionCode` (and `versionName`) in `app/build.gradle.kts` and sign with the same upload key.
