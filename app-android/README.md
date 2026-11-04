# Plainly: the Android app for the Blog Digest guides

A native Jetpack Compose app (no WebView) that reads https://randomthoughtsever.blogspot.com/ through Blogger's public
JSON feed and presents it with its own design, plus on-device tools and calculators.

- **Today**: greeting, auto-advancing featured carousel, continue reading, quick tools, topics, latest (paged, pull to refresh).
- **Explore / Topic / Search**: topic tiles, per-topic feeds, debounced search.
- **Reader** (`ui/Reader.kt`): article HTML is parsed with jsoup into native blocks (headings, paragraphs with links,
  Quick answer card, lists, tables, FAQs, images). Scripts, styles and the site's web widgets are dropped; the matching
  native tool is offered instead. Links to other posts open in the app; external links open in Custom Tabs.
- **Tools** (`tools/`): photo resizer to a KB target (ImageDecoder, quality search), PDF compressor (PdfRenderer plus a
  small JPEG-in-PDF writer), QR codes (ZXing; link, text, Wi-Fi), and loan/mortgage, investment growth, credit card
  payoff, FIRE and emergency fund calculators with animated charts and 16 currencies.
- **Saved**: bookmarks with the full text kept on the phone for offline reading, plus history. Light and dark themes,
  text size, Google Play in-app review prompt.

Application ID `com.randomthoughtsever.plainly`; minSdk 29, targetSdk/compileSdk 36; AGP 8.11, Kotlin 2.1, Compose BOM 2025.05.
Store assets and listing text: `store/`. Publishing steps: `PLAY_STORE_GUIDE.md`.

## Build and test

1. Android SDK with `platforms;android-36` and `build-tools;36.0.0`; put `sdk.dir=/path/to/sdk` in `local.properties`.
2. Signing: create `keystore.properties` (never commit it) with `storeFile`, `storePassword`, `keyAlias`, `keyPassword`.
3. `./gradlew bundleRelease assembleRelease` writes `app/build/outputs/bundle/release/app-release.aab` (upload this to
   Play) and `app/build/outputs/apk/release/app-release.apk` (for testing on a phone).
4. `./gradlew testDebugUnitTest` runs:
   - `LogicTest`: calculator maths, PDF cross-reference table, Wi-Fi QR escaping, and the reader against sample HTML
     and every live post (it checks that no script or CSS text leaks into the article).
   - `ScreenshotTest`: Robolectric + Roborazzi render the real app with live content into `app/build/shots/`.
     `python3 store/make_screenshots.py` frames them as the Play Store screenshots.

For each update, raise `versionCode` (and `versionName`) in `app/build.gradle.kts` and sign with the same upload key.

## Security and privacy

- No login, accounts, analytics or ad SDKs. Permissions: INTERNET and ACCESS_NETWORK_STATE only.
- No WebView and no remote code: articles are parsed into native text, so the site's scripts never run.
- HTTPS only (network security config, cleartext off, system CAs only). Deep links are accepted only for https URLs on the blog's host.
- Files are processed on the device. Saving uses MediaStore (no storage permission). Sharing goes through a FileProvider
  that exposes only the app's `cache/share` folder.
- No backups or device transfer of app data. Release builds are minified with R8.
