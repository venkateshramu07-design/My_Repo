# Plainly: the Android app for the Blog Digest guides

A native Jetpack Compose app (its screens use no WebView) that reads https://randomthoughtsever.blogspot.com/ through Blogger's public
JSON feed and presents it with its own design, plus on-device tools and calculators.

- **Today** (`ui/HomeScreen.kt`): a finite page. Topic stories (full-screen, auto-advancing quick answers), a daily
  pick, just published, a live "money in 10 seconds" slider, flip-card quick answers, popular tools, a topic spotlight
  and "More to read" (every guide, newest first, mixed by topic, paged as you scroll). It reloads on open and resume, checks for a new post every minute while visible, and
  `data/NewPosts.kt` (WorkManager, every 15 min) posts a notification for new guides. Cover art comes live from the
  website's design layer (`version.txt` → `bd-redesign.js` → `BD_COVERS`), on top of the copy built into the app.
- **Navigation**: floating animated bar with Today, Explore, Reads, Community, Tools, You and Games (`ui/App.kt`).
- **Games** (`ui/Games.kt`): Rock Paper Scissors, Money Match, Number Rush, Tic-Tac-Toe (minimax "Unbeatable") and Reaction
  Time; best scores in SharedPreferences. `LogicTest.gamesFollowTheirRules` checks the rules, 500 maths questions and that
  Unbeatable never loses any possible game.
- **Personal layer** (`data/Profile.kt`, all on the phone): first-launch onboarding (`ui/Onboarding.kt`: goals, country and
  currency, optional numbers, then a plan), check-ups (`data/Checks.kt`, `ui/CheckScreen.kt`: Money Health, Stress and
  Career checks with a score, weakest area, fixes and a share picture from `ui/ShareCards.kt`), My numbers, Your plan,
  Daily 3 with a breathing minute and quiz, 31 quizzes tied to guides, challenges with an evening reminder
  (`data/Reminders.kt`), badges and a knowledge map (`data/Engage.kt`, `ui/Personal.kt`), Fix/Decide/Learn/Facts labels,
  Listen (`ui/Speaker.kt`, Android text-to-speech) and a home-screen widget (`PlainlyWidget.kt`).
- **Community** (`data/Community.kt`, `ui/CommunityScreen.kt`): Firebase anonymous sign-in and Cloud Firestore over REST
  (no Firebase SDK). Posts, likes, comments, guide discussions, quiz polls, a streak leaderboard, report/block/delete and
  "Delete my community data". `firebase/firestore.rules` enforces ownership, one like and one vote per person, and
  counters that can't be faked. Settings come from `community.properties` (`projectId`, `apiKey`). `CommunityTest` runs
  everything, including cheating attempts, against the Firebase emulators (`cd firebase && firebase emulators:start
  --only auth,firestore --project demo-plainly`).
- **Explore** (`ui/ExploreScreen.kt`, `ui/Collections.kt`): built from `Repository.index()`, an index of every guide from the
  summary feed (cached, refreshed every 10 minutes). It has search suggestions, Surprise me, collections defined as rules
  (so new posts join them by themselves), 35 countries with flags, topic counts, and the A-to-Z library.
- **Reads** (`ui/QuickReads.kt`): a vertical pager of quick answers, "For you" or by group. **You** (`ui/YouScreen.kt`):
  streak, stats, topics read, saved, history and settings.
- **Search**: Blogger finds posts and `data/SearchRank.kt` orders them by relevance; matching tools are shown first.
- **Reader** (`ui/Reader.kt`): article HTML is parsed with jsoup into native blocks (headings, paragraphs with links,
  Quick answer card, lists, tables, FAQs, images). Scripts, styles and the site's web widgets are dropped; the matching
  native tool is offered instead. Links to other posts open in the app; external links open in Custom Tabs.
- **All 72 tools** (`tools/Catalog.kt`, `tools/WebTool.kt`): `assets/tools.json` (made by `tools-catalog/make_catalog.py`)
  lists the website's 64 interactive tools. `WebTool` lifts only the tool (its markup, styles and scripts) out of the
  post and runs it in a WebView with the app's colours. A bridge (`addWebMessageListener`, blog origin only) passes
  downloads, printing and copying to the phone. Posts labelled Tools or Calculators published after the list was made
  are picked up live. `WebToolPagesTest` + `tools-catalog/check_webtools.js` run every tool in Chromium and report script errors.
- **Native tools** (`tools/`): photo resizer to a KB target (ImageDecoder, quality search), PDF compressor (PdfRenderer plus a
  small JPEG-in-PDF writer), QR codes (ZXing; link, text, Wi-Fi), and loan/mortgage, investment growth, credit card
  payoff, FIRE and emergency fund calculators with animated charts and 16 currencies.
- **Saved**: bookmarks with the full text kept on the phone for offline reading, plus history. Light and dark themes,
  text size, Google Play in-app review prompt.
- **Ads**: labelled inline adaptive banners between content (home, topic lists, articles, under calculators) and an
  interstitial after a finished tool task (from the second, at most every 4 minutes). Banners collapse when empty.

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

- No login, accounts or analytics. Ads: Google Mobile Ads SDK (AdMob) with Google's UMP consent form, started only
  when consent rules allow (`ads/Ads.kt`). Permissions: INTERNET, ACCESS_NETWORK_STATE and the advertising ID (added by the ads SDK).
- AdMob IDs come from `admob.properties` (`appId`, `banner`, `interstitial`). Without it, Google's sample IDs are used.
  Debug builds always use the sample ad units, and `bundleRelease` fails without real IDs, so a Play upload can't carry sample ads.
- Articles are parsed into native text, so the site's scripts never run there. Website tools run in a WebView limited
  to the tool's own code, served as the blog's origin. Navigation is blocked (links open in the app or a Custom Tab),
  there's no file:// access, and the bridge accepts only messages from the blog's origin and saves only allowlisted file types.
- HTTPS only (network security config, cleartext off, system CAs only). Deep links are accepted only for https URLs on the blog's host.
- Files are processed on the device. Saving uses MediaStore (no storage permission). Sharing goes through a FileProvider
  that exposes only the app's `cache/share` folder.
- No backups or device transfer of app data. Release builds are minified with R8.
