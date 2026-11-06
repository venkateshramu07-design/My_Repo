# Plainly 1.1.0: pre-launch test report

Version under test: 1.1.0 (versionCode 2), branch `claude/google-keys-indexing-compare-1oc3cu`, 6 November 2026.

## Summary

| Area | Result |
|---|---|
| Automated tests (unit, screen, black-box QA) | **74 tests: 70 passed, 0 failed, 4 skipped** (3 for the optional Firebase community, which needs its emulators; 1 monkey run marked inconclusive: a test-sync timeout after 257 random taps, with no app error) |
| Static analysis (Android Lint, release) | **0 errors** (was 5; all fixed). Remaining warnings are style hints (KTX helpers, dependency updates). |
| Kotlin compiler warnings | **0** (deprecated icons and APIs replaced) |
| Security and release-package audit | **Pass** (details below) |
| Defects found and fixed in this round | **10**, including 1 crash on Android 10–11 |
| Real-device testing | **Not possible here** (no emulator support in this environment). Use the checklist below and Play's pre-launch report. |

## How it was tested

- **White-box (code is known):** unit tests of the logic (XP, levels, streak freezes, puzzles, simulator, reminders,
  recaps, translation mapping, support rules, calculators, PDF writer, reader parser), date edge cases, Lint, compiler
  warnings, a review of what the code shrinker (R8) keeps, and a manifest/permission/WebView/network review.
- **Black-box (the app as a user sees it):** the real app, rendered on a virtual phone with Robolectric and driven
  through its screens: every screen on the oldest and a recent Android, a 300-tap random "monkey", no internet, damaged
  saved data, links from outside, large text, a screen-reader label audit, and screenshots of every main screen.
- **Live content:** every guide the app links to by id, and every topic rule, checked against the live blog.

Tools: JUnit 4, Robolectric 4.16 (Android 10 / API 29 and Android 15 / API 35), Compose UI test, Roborazzi screenshots,
Android Lint (AGP 8.11), apksigner, aapt2, dexdump.

## Test suites

| Suite | Tests | What it covers |
|---|---|---|
| `LogicTest` | 29 | Calculators, PDF writer, QR escaping, article reader on every live post, search ranking, check-ups, quizzes and badges, games' rules (500 maths questions; unbeatable tic-tac-toe never loses), blog comments, XP/levels/leagues/Daily Run/cards/check-ins/"I tried this", daily puzzles (400 days each), money simulator (200 seeded years), reminders, seasons, question of the week, tips rules, translation keeps structure, new puzzles and archive, streak freezes, calendar and number-format edge cases |
| `ReleaseQaTest` | 8 | Every screen opens on Android 10 and on Android 15 (45 screens each); 300-tap seeded monkey; no internet; corrupted saved data; deep-link allowlist (8 allowed, 16 hostile inputs refused); screen-reader label audit on 13 screens; live content integrity |
| `ScreenshotTest` + `BlogCommunityScreenTest` | 26 + 1 | The real app on a virtual phone: onboarding, Today, stories, articles, Explore, Reads, tools, games, community (blog comments and joining), challenges, the play layer (Daily Run, puzzle, simulator, levels, calm, recap), support, translation picker, first session, search while typing, small-phone tab bar, 160% text |
| `CoverTest`, `SceneTest`, `WebToolPagesTest` | 8 | Cover images for new posts; every topic animation renders; all 64 website tools load without script errors |
| `CommunityTest` (+ `ScreenshotTest.communityFeed`) | 2 + 1 (skipped) | The optional Firebase community, run against the Firebase emulators. Skipped because the community runs on blog comments; they run when the emulators are started. |

## Defects found and fixed

| # | Severity | Found by | Defect | Fix |
|---|---|---|---|---|
| 1 | **Crash** | Lint (NewApi) | Finishing a daily puzzle crashed on **Android 10 and 11**: the "next puzzle in" countdown used `Duration.toMinutesPart()`, which only exists on Android 12+ | Plain arithmetic |
| 2 | High | Monkey test | Tapping a link to another guide inside an article could switch screens off the main thread | Navigation always runs on the main thread |
| 3 | Medium | Live content test | The simulator's "phone" event and one daily action pointed at topics no guide covers, so their "Learn" links never appeared | Rules point at existing guides |
| 4 | Medium | Accessibility audit | Daily Run step bars, colour-theme circles and the three Settings switches had no screen-reader label | Labels added |
| 5 | Medium | Large-text test (earlier round) | Tab labels overlapped and two cards broke words at 160% text | Fixed layouts |
| 6 | Low | Lint (MissingPermission) | Notifications posted without an explicit permission check at the call | Checked before posting |
| 7 | Low | Lint | Animated offsets recomposed every frame (battery) | Lambda offsets |
| 8 | Low | Lint / compiler | Icons that don't mirror for right-to-left languages; deprecated APIs | AutoMirrored icons, current APIs |
| 9 | Low | Lint | Unhandled install states of the on-demand translator | Shown as "Installing the translator…" |
| 10 | Low | Order-dependent test failure | The Google Play billing connection stayed open after leaving the Support screen | Closed when the screen goes away (Google's recommended practice) |

Test-only problems fixed along the way (not app defects): pressing Back from the wrong thread in the test helper; a
level-up dialog covering a tap; a final check that didn't allow for an open dialog. Two environment notes:
- The billing library can't connect in the JVM test environment (there's no Google Play app), so tests skip billing,
  as they already skip the image loader. Tips are covered by the real-phone checklist (item 9).
- One test class left shared state that made a later community test stall when the whole suite ran in one JVM. The
  exact leftover wasn't pinned down; that test passes on its own every time (5 of 5), and each test class now runs in
  a fresh JVM (`forkEvery = 1`), which is how the app really starts. Worth watching in Play's pre-launch report.
- A cover image once returned "not found" from Google's image server during a run; it loads normally (re-run passed).
- Intermittent (about 2 of 10 full runs): with the comment box ("Join the discussion") open, the virtual phone's
  "wait until the screen is still" check timed out. The app threw no error and never crashed, and opening the box
  directly passes every time (5 of 5). Investigated: it happens when a conversation finishes loading while the box is
  open; with the box open the main thread had nothing queued and the screens' redraw counters stayed constant (no redraw
  loop), so it points to the test framework's idle detection rather than the app. Not proven on a real phone. The monkey test reports this case as *inconclusive (skipped)*, not as a pass, and it's
  on the real-phone checklist (item 8).

## Security and release audit

- Signed with the upload key (APK Signature Scheme v3, valid from Android 9; the app needs Android 10+). Not debuggable.
- Exported components: only the launcher activity, plus WorkManager's diagnostics receiver, which Android protects with
  the system-only DUMP permission.
- Network: HTTPS only (cleartext off), system certificate authorities only.
- WebView (website tools): no file access; the JavaScript bridge accepts messages only from the blog's origin; no SSL-error overrides.
- No signing passwords, private keys, API keys or tokens inside the APK; `keystore.properties`, `admob.properties` and
  the `.jks` key have never been committed to git.
- Backups and device-to-device transfer are off (no personal data leaves the phone that way).
- Code shrinker keeps everything Android creates by name: background workers (with their constructors), widgets, the
  Application class, and the on-demand translator.
- Payments: PayPal/UPI details are not compiled into Play builds; `bundleRelease` refuses `-PsupportLinks=true`.
- 16 KB memory-page alignment of native code: verified. Billing Library 8. Target API 36.

## Not covered here (needs a real phone)

This environment can't run an Android emulator, so these weren't exercised on real hardware:
- The minified release build running on a phone (it was inspected, not run).
- Ads (real AdMob fill), Google Play billing (tips), Play's on-demand download of the translator, ML Kit translation
  quality, text-to-speech voices, notifications arriving on time, home-screen widgets, sharing to other apps,
  Custom Tabs sign-in for community posts, camera/photo pickers, performance and battery on low-end phones.

## Real-phone checklist (do before going public)

Install from a **Play internal testing** track (so billing and on-demand delivery work), on at least one Android 10–11
phone and one Android 14–16 phone. Tick each:

1. **First run:** onboarding → plan → "Your first 3 minutes" shows; complete all 3 steps → celebration, +100 XP, streak freeze.
2. **Today:** stories open and covers show in full; pull to refresh; the Daily Run takes about 5 minutes and ends with "Done for today".
3. **Puzzle:** play today's puzzle and finish it on the **Android 10–11 phone** (the fixed crash); share the result; open a past puzzle.
4. **Article:** opens fast; Listen works; select text → "Save to notebook"; quick checks; reach the end → card reveal.
5. **Translate:** tap 文A → pick Hindi or Spanish → "Getting the translator" (Play download) → language download → translated
   article; "Original" switches back; Arabic reads right to left.
6. **Tools:** photo resize to a KB target, PDF compress, QR code; files save and share.
7. **Games:** each of the five; A Year of Money to the end; Calm corner sounds play and stop when you leave.
8. **Community:** join; open a conversation; "Copy and continue" opens the blog's comment box; post a comment and see it
   appear marked "(you)"; Report opens the contact form. Also open a guide, scroll to Discussion, tap **Join in** right
   after the "+20 XP" pop-up appears, and check the post box opens and you can type (see the note under Test-only problems).
9. **Tips:** Support Plainly shows Google Play prices (after creating the 3 tip products); a test purchase with a licence
   tester account shows the thank-you.
10. **Ads:** banners appear and are labelled; an interstitial appears only after finishing a tool task, at most every 4 minutes.
11. **Notifications:** allow them; publish a test post or wait for the evening reminder; tapping opens the right screen.
12. **Widgets:** add both widgets; answer the quiz from the home screen; the app shows the XP.
13. **Settings:** dark mode, text size, sounds, article language, delete downloaded languages, privacy choices.
14. **Offline:** airplane mode → the app opens, says it's offline, puzzles/simulator/calm/games still work.
15. **Accessibility:** turn on TalkBack and the largest font → tabs, Daily Run, puzzle and games are readable and usable.

Then let **Play's pre-launch report** run (it tests the upload automatically on real devices in Google's lab, with
crash, accessibility and security checks), and fix anything it flags before promoting to production.
