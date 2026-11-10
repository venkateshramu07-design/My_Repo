# Plainly: checklist for every future build

Follow this for every update sent to Google Play. Lessons from 1.1.0–1.1.3 are marked **(lesson)**.

## 1. Before you start: things that must exist (never in git)

| File | What it is | If lost |
|---|---|---|
| `blog-digest-upload.jks` (from the backup zip `Plainly-signing-key-KEEP-PRIVATE.zip`) | Upload key, alias `upload`, SHA-256 starts `12:E1:9C:32` | Play Console → App integrity → request an upload key reset (takes days). Keep the zip in two safe places (for example Google Drive and a USB stick). |
| `app-android/keystore.properties` | `storeFile` (path to the .jks), `storePassword`, `keyAlias`, `keyPassword` | Recreate from the zip's notes |

**Cloud sessions start empty.** In a new Claude session the key isn't there. Give the session the key zip, and it recreates `keystore.properties` pointing at the extracted .jks. Do the same for the three lines of `admob.properties`.
| `app-android/admob.properties` | `appId`, `banner`, `interstitial` (real AdMob IDs) | Copy again from AdMob → Apps → Plainly |
| `app-android/translate/` | The on-demand translator module | It's in git |

These are in `.gitignore`. Never commit them, and never commit `.aab` or `.apk` files.

## 2. Version number (required every time)

In `app-android/app/build.gradle.kts`:
- `versionCode` must be **higher than any code ever uploaded**, including drafts and discarded releases. Last used: **7** (1.1.5). Next: **8**.
- `versionName`: 1.1.4 for fixes, 1.2.0 for new features.

Play rejects a reused version code ("Version code X has already been used").

## 3. Build

```
cd app-android
./gradlew :app:bundleRelease
```
Output: `app/build/outputs/bundle/release/app-release.aab`. Copy it as `download/plainly-<version>-release.aab`.

The build stops by itself if:
- `admob.properties` is missing (it would ship sample ads).
- It's built with `-PsupportLinks=true` (PayPal/UPI links aren't allowed in Play builds; tips go through Play Billing).
- **(lesson)** The base app declares a component whose class only exists in the on-demand translator module. On a Play install that stops the app at launch, which is what broke 1.1.0–1.1.2. If this check fails, don't remove the check: remove the component from the merged manifest with `tools:node="remove"` (see `translate/src/main/AndroidManifest.xml`) or move the code into the base app.

## 4. Automated tests

```
./gradlew :app:testDebugUnitTest
```
Expect all to pass, except the 3 Firebase tests (skipped without emulators) and the monkey run, which may report "inconclusive". If you changed the translator, start-up or `MainActivity`, the minimum is `LogicTest`, `CrashReportTest` and `ReleaseQaTest`.

**(lesson)** These tests run the app with every part built in. They **cannot** catch problems that only appear when Google Play delivers the app in pieces. Step 6 covers that.

## 5. Check the bundle

- It's signed with the upload key: `keytool -printcert -jarfile <aab>` shows SHA-256 `12:E1:9C:32…`.
- The version code is right: it's in the merged manifest under `app/build/intermediates/merged_manifest/release/`.
- Size is about 39 MB as an .aab. Play's download is about 4–5 MB, and the translator about 7 MB on demand. A big jump means something was added by mistake.

## 6. Test the real Play install (never skip) **(lesson)**

A direct-install APK is **not** the same as a Play install. Play splits the app into pieces (base, screen size, language, processor, translator on demand). Only a Play install tests that.

1. Upload to **Closed testing → Alpha → Create new release** (or Internal testing for a faster check), then send for review.
2. When it says "Available to testers", **uninstall** any direct-install copy, then install from the Play test link.
3. Check that the Play details page shows the new version.
4. On at least one phone (ideally one Android 10–11 and one Android 14+):
   - The app **opens** from the home-screen icon.
   - **Translate** works the first time: article → 文A → a language → "Getting the translator" → translated text.
   - Today, an article, the puzzle, one tool and Settings all open.
   - If anything new was added, test it.
5. If the app closes, open it again. The **"Plainly closed unexpectedly"** screen shows the error with a **Copy** button. Send that text.
6. Check **Pre-launch report** and **Android vitals → Crashes and ANRs** in Play Console for the new version.

## 7. Google Play rules to keep in mind

- **Target API level:** Google requires a recent `targetSdk` every year (usually by 31 August). The app currently targets 36.
- **Play Billing Library:** keep it current. Version 7 stopped being accepted; the app uses 8.
- Changing ads, data collected, permissions or features means updating **Data safety**, the **Ads** declaration and the **privacy policy** post on the blog to match.
- Keep **Target audience: 18 and over** (avoids the Families policy).
- Store listing changes (text, screenshots) also go through review.

## 8. Releasing

- **Closed testing → production:** after the 14 days with 12+ testers, use **Apply for production**, then promote the same release.
- **Production updates:** use a **staged rollout** (start at 10–20%), watch Android vitals for a day or two, then increase to 100%.
- Write short release notes ("Fixes and improvements" is fine).

## 9. After a release

- Commit and push the code change. Note the new version code at the top of section 2.
- Keep the uploaded `.aab` in `download/` until the next release.
- Watch reviews and Android vitals for a few days.
