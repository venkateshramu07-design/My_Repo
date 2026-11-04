# How to put Plainly on Google Play: step by step

You don't need to know app development. The app is built and signed. What's left are the steps only the account
owner can do in Google Play Console. Allow about 3 weeks in total, mostly waiting: about 2 days for account checks,
the 14-day test that Google requires for new personal accounts, then up to a week for review.

## What you have

| File | What it's for |
|---|---|
| `plainly-1.0.0.aab` | **The file you upload to Google Play.** |
| `plainly-1.0.0-test.apk` | To try the app on your own Android phone first. Don't upload this one. |
| `plainly-upload.jks` + `SIGNING-KEY.txt` | Your private upload key and its passwords. **Keep both safe** (for example in Google Drive and on a USB stick). Every future update must be signed with this key. Never share them publicly. |
| `store/` folder | Icon, feature graphic, 8 screenshots and all the listing text (`store/listing.md`). |

App details: name **Plainly**, store title **"Plainly: Money, Mind & Work"**, package `com.randomthoughtsever.plainly`,
version 1.0.0 (code 1). It targets Android 16 (API 36), as Google Play requires, and runs on Android 10 and newer.

## What the app is

Plainly is a native Android app with its own screens. It is not a copy of the website in a browser window:
- **Today**: greeting, a featured carousel that moves on by itself, continue-reading, quick tools, topics and the latest guides.
- **Explore**: colourful topic tiles and search.
- **Reader**: each article is rebuilt as native text, with a Quick answer card, tables, lists and FAQs, adjustable text
  size, a reading-progress bar, bookmarks, sharing and links between articles that stay inside the app.
- **Tools**, all running on the phone: photo resizer to an exact KB size, PDF compressor, QR code maker (links, text and
  Wi-Fi), and loan/mortgage, investment growth, credit card payoff, FIRE and emergency fund calculators with live charts
  and 16 currencies.
- **Saved**: saved articles readable offline, plus reading history.
- Light and dark mode. No account, no ads, no tracking, no storage permission.

Anyone can use it: people install it and it opens straight away. The upload key is only for you as the publisher.

---

## Step 1: Try the app on your phone (10 minutes)

1. Send `plainly-1.0.0-test.apk` to your Android phone (email it to yourself or put it in Google Drive) and tap it.
2. Android will say it's blocked for security. Tap **Settings**, turn on **Allow from this source**, go back and tap **Install**.
3. Open **Plainly** and check:
   - **Today** loads the latest guides with their covers. Swipe the featured cards, and pull down to refresh.
   - Open a guide. Tap the **bookmark**, change the **text size**, and tap a link to another guide (it opens inside the app).
   - **Tools → Photo resizer**: pick a photo, choose 100 KB, tap **Resize**, then **Save** (it goes to Pictures/Plainly).
   - **Tools → Loan & mortgage calculator**: change the numbers and watch the chart update. Tap **Currency** to change it.
   - **Saved**: your bookmarked guide is there. Turn on aeroplane mode and open it: it still reads.
   - **Settings** (gear icon on Today): try Dark mode.
4. Before you install the Play Store version later, uninstall this test copy (the Play version replaces it).

## Step 2: Create a Google Play developer account (1-2 days, US$25 once)

1. Go to **https://play.google.com/console/signup** and sign in with the Google account you want to own the app.
2. Choose **Yourself** (a personal account).
3. Pay the one-time **US$25** registration fee.
4. Enter your legal name and address, and verify your **phone number** and **email**.
5. Complete **identity verification** by uploading a government photo ID. Google usually confirms within 1-2 days by email.
6. Google may also ask you to prove you have an Android phone, by installing the **Play Console** app on it and signing in.

## Step 3: Create the app (5 minutes)

In Play Console, click **Create app** and enter:
- App name: **Plainly: Money, Mind & Work**
- Default language: **English (United States) – en-US**
- App or game: **App**
- Free or paid: **Free**
- Tick the two declarations (Developer Program Policies and US export laws), then click **Create app**.

## Step 4: Fill in "Set up your app" (about 20 minutes)

On the app's **Dashboard**, open each task under **Set up your app** and answer as follows.

| Task | What to enter |
|---|---|
| **Privacy policy** | `https://randomthoughtsever.blogspot.com/2021/06/privacy-policy.html` (its "Plainly, Our Android App" section describes this app) |
| **App access** | All functionality is available without special access |
| **Ads** | No, my app does not contain ads |
| **Content rating** | Start the questionnaire. Enter your email and choose the category **"Reference, News, or Educational"**. Answer **No** to every question (violence, sexuality, language, drugs, gambling, user interaction, sharing location, digital purchases). Submit. You'll get a rating such as "Everyone" or "Rated for 3+". |
| **Target audience** | Tick **18 and over** only (the money and health guides are written for adults, and this avoids the extra rules for children's apps). |
| **News apps** | No |
| **Data safety** | See below |
| **Government apps** | No |
| **Financial features** | "My app doesn't provide any financial features" (the calculators only do arithmetic: no loans, payments, trading or banking) |
| **Health apps** | None of these |
| **Advertising ID** | No |

**Data safety answers:**
1. "Does your app collect or share any of the required user data types?" Answer **No**.
   (Saved articles, history and settings never leave the phone; photos and PDFs are processed on the phone; there are
   no accounts, analytics or ads. Data that stays on the device doesn't count as "collected" under Google's rules.)
2. Click **Next**, review the summary ("No data collected, no data shared") and **Save**.

## Step 5: Store listing (15 minutes)

All the text is in `store/listing.md`.

1. Go to **Grow users → Store presence → Main store listing**.
2. Paste the **App name** (`Plainly: Money, Mind & Work`), the **Short description** and the **Full description**.
3. Upload:
   - **App icon**: `store/play-icon-512.png`
   - **Feature graphic**: `store/feature-graphic-1024x500.png`
   - **Phone screenshots**: the 8 files in `store/screenshots/`, in order (screenshot-1 … screenshot-8).
4. Click **Save**.
5. Go to **Store settings**: set the app category to **Finance**, add the tags listed in `store/listing.md`, enter
   your contact email and website, and **Save**.

## Step 6: Closed test with 12 testers for 14 days (required for new personal accounts)

Google requires every new personal developer account to test with **at least 12 people for 14 days in a row** before
the app can be published.

1. Go to **Test and release → Testing → Closed testing**. Open the default track ("Closed testing - Alpha") or click **Create track**.
2. Under **Testers**, create an email list, add the Gmail addresses of **at least 12 people** with Android phones
   (friends and family; add 15 or so in case someone drops out), and save.
3. Under **Countries / regions**, add all countries.
4. Click **Create release**:
   - When asked about app signing, keep **Play App Signing** with a Google-generated key (the recommended default).
   - **Upload `plainly-1.0.0.aab`.**
   - Release name: `1.0.0`. Release notes: `First release of Plainly.`
5. Click **Next**, then **Save**, then **Send for review** (in **Publishing overview**). The first review usually takes from a few hours to a few days.
6. When it's approved, copy the **opt-in link** from the Testers tab and send it to your testers. Each tester opens the
   link, taps **Become a tester**, installs Plainly from Google Play and keeps it installed.
7. Wait **14 days** with at least 12 testers opted in the whole time. Ask them to open the app every few days.

## Step 7: Go public (production)

1. After 14 days, the Dashboard shows **Apply for production**. Click it and answer the short questions about your
   test, for example: "12+ friends and family used the reader, saved articles offline, the calculators and the photo,
   PDF and QR tools on different phones for 14 days. No crashes were reported."
2. When Google approves (often within 7 days), go to **Test and release → Production → Countries / regions** and add
   all countries. Then click **Create release**, choose the same build from the library, add release notes, and click
   **Next → Save → Send for review**.
3. After the final review, Plainly is live worldwide at
   `https://play.google.com/store/apps/details?id=com.randomthoughtsever.plainly`

## If Google rejects the app

Copy the full text of Google's email and send it to me. I'll make the change and build a new version. The app has its
own screens, offline reading, on-device tools and calculators, so the usual "this is just a website" rejection
(minimum functionality) shouldn't apply.

## Updating the app later

Ask me for an update. I'll change the app, raise the version number and build a new `.aab` signed with your key. You
upload it under **Production → Create release**. Keep `plainly-upload.jks` and its passwords safe. If they're lost,
Google Play support can reset the upload key, but that takes a few days.
