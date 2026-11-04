# Plainly: the Android app for the Blog Digest website

A native Android shell for https://randomthoughtsever.blogspot.com/ with bottom tabs (Home, Money, Mind, Career, Tools),
file picking and saving for the blog's in-browser tools (blob/data downloads saved to Downloads via MediaStore),
pull-to-refresh, offline screen and cache, external links in Custom Tabs, ads blocked, splash screen and adaptive icon.

- Application ID: `com.randomthoughtsever.plainly` (code namespace `com.randomthoughtsever.blogdigest`); minSdk 29, targetSdk/compileSdk 36; AGP 8.11, Kotlin 2.1, Gradle 8.14.
- Code: `app/src/main/java/com/randomthoughtsever/blogdigest/MainActivity.kt`.
- Store assets and listing text: `store/`. Publishing steps: `PLAY_STORE_GUIDE.md`.

## Build

1. Android SDK with `platforms;android-36` and `build-tools;36.0.0`; put `sdk.dir=/path/to/sdk` in `local.properties`.
2. Signing: create `keystore.properties` (never commit it) with `storeFile`, `storePassword`, `keyAlias`, `keyPassword`.
3. `./gradlew bundleRelease assembleRelease`, which writes `app/build/outputs/bundle/release/app-release.aab` (upload this to
   Play) and `app/build/outputs/apk/release/app-release.apk` (for testing on a phone).

For each update, raise `versionCode` (and `versionName`) in `app/build.gradle.kts` and sign with the same upload key.

## Security

- No login, accounts, analytics or ad SDKs; permissions are only INTERNET and ACCESS_NETWORK_STATE.
- The WebView opens only `https://randomthoughtsever.blogspot.com` pages; other links go to Custom Tabs or, for an allowlist
  of schemes (mailto, tel, sms, whatsapp, tg, upi, geo, market), to other apps. `intent:`, `file:`, `content:` and
  `javascript:` links are ignored. Deep links are accepted only for https URLs on the blog's host.
- No `addJavascriptInterface`: the page reaches the app only through `WebViewCompat.addWebMessageListener`, restricted to
  the blog's origin and its main frame, and injected scripts run only on that origin.
- Saving files: allowlisted types (PDF, images, ZIP, CSV, text, JSON), sanitised names, 100 MB cap, written to Downloads via
  MediaStore (no storage permission).
- HTTPS only (network security config, cleartext off, system CAs only), mixed content blocked, Safe Browsing on, SSL errors
  never bypassed, file access off, geolocation off, third-party cookies off, multiple windows off.
- No backups or device transfer of app data; WebView metrics opt-out; release builds are minified with R8.
