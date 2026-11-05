# How to put Plainly on Google Play: step by step

You don't need to know app development. The app is built and signed. What's left are the steps only the account
owner can do in Google Play Console. Allow about 3 weeks in total, mostly waiting: about 2 days for account checks,
the 14-day test that Google requires for new personal accounts, then up to a week for review.

## What you have

| File | What it's for |
|---|---|
| `plainly-1.1.0.aab` | **The file you upload to Google Play.** I build it once you send me your AdMob IDs (Step 2B). |
| `plainly-1.1.0-preview.apk` | To try the app on your own Android phone first. It shows Google's sample "Test Ad" banners in the real ad spots. Don't upload this one. |
| `plainly-upload.jks` + `SIGNING-KEY.txt` | Your private upload key and its passwords. **Keep both safe** (for example in Google Drive and on a USB stick). Every future update must be signed with this key. Never share them publicly. |
| `store/` folder | Icon, feature graphic, 8 screenshots and all the listing text (`store/listing.md`). |

App details: name **Plainly**, store title **"Plainly: Money, Mind & Work"**, package `com.randomthoughtsever.plainly`,
version 1.1.0 (code 2). It targets Android 16 (API 36), as Google Play requires, and runs on Android 10 and newer.

## What the app is

Plainly is a native Android app with its own screens. It is not a copy of the website in a browser window:
- **Today**: a finished page, not an endless feed. It has topic **stories** you tap through (a ring lights up when a topic
  has something new), **Today's pick** (changes every day), **Just published**, a live **Money in 10 seconds** slider,
  flip-card **30-second answers**, a topic spotlight, then **More to read**: every guide on the blog, newest first and mixed by topic, loading as you scroll. Today shows articles only; pages
  labelled Tools or Calculators appear in the Tools tab instead.
- **New posts appear straight away**: Today reloads whenever the app is opened or brought back, checks every minute while
  it's on screen (a "Just published" pill slides in), and pull-to-refresh is always there. People who allow
  notifications get a **"New on Plainly"** notification within about 15 minutes of a post going live (Android's limit for
  background checks). Cover art for new posts comes live from the website's design layer.
- **Bottom bar** (floating, animated): **Today · Explore · Reads · Tools · You**.
- **Explore**: search with suggestions, a "Surprise me" button, collections that fill themselves (Start here, Get out
  of debt, Invest & retire early, Countries in numbers, Passport & visa photos, and more), all 35 countries with flags,
  every topic with its real number of guides and newest title, and all guides from A to Z.
- **Reads**: swipe up through quick answers, one guide per screen, filtered "For you" (the topics you read most) or by
  Money, Mind, Work and World. Save, share or open the full guide.
- **You**: your reading streak, guides read, saved and files made, the topics you read most, saved guides, history,
  light/dark mode and settings.
- **Reader**: each article is rebuilt as native text, with a Quick answer card, tables, lists and FAQs, adjustable text
  size, a reading-progress bar, bookmarks, sharing, and links between articles that stay inside the app. If the post has
  a tool, a "Try it" button opens it.
- **Tools: all 72**, with search and categories:
  - 8 built into the app that also work offline: photo resizer, PDF compressor, QR codes, and loan, investment growth,
    card payoff, FIRE and emergency fund calculators.
  - The website's 64 interactive tools, for example the passport photo maker, PDF merge and sign, resume and invoice
    makers, currency converter, rent-or-buy and debt planners, and the time zone planner. Each opens full screen inside
    the app with the app's colours. Only the tool is shown, without the article or the website's menus or ads. Files they
    make are saved to the phone.
  - **New tools appear automatically**: publish a post with the label **Tools** or **Calculators** and it shows up in
    the app's "New" section.
- Light and dark mode. No account and no storage permission.
- **Ads (Google AdMob)**, kept few and clearly labelled "Advertisement":
  - one banner on Today between Topics and Latest, and one after the 8th guide in Latest;
  - one in each topic list, after the 6th guide;
  - in articles: one after the first few paragraphs, one two-thirds through long articles (never straight after a
    heading), and one before "More in…";
  - one at the bottom of each calculator, below the result;
  - one full-screen ad at most every 4 minutes, only after someone saves a file in a tool, and never the first time.
  - No ads on Saved, Settings, Search or Explore, and none next to buttons.
  - Where the law requires it (EEA, UK, Switzerland and some US states), Google's consent form appears first, and
    **Settings → Privacy choices** lets people change their answer. Ad space collapses when there is no ad or no internet.

Anyone can use it: people install it and it opens straight away. The upload key is only for you as the publisher.

---

## Step 1: Try the app on your phone (10 minutes)

1. Send `plainly-1.1.0-preview.apk` to your Android phone (email it to yourself or put it in Google Drive) and tap it.
2. Android will say it's blocked for security. Tap **Settings**, turn on **Allow from this source**, go back and tap **Install**.
3. Open **Plainly** and check:
   - **Today** loads the latest guides with their covers. Swipe the featured cards, and pull down to refresh.
   - Open a guide. Tap the **bookmark**, change the **text size**, and tap a link to another guide (it opens inside the app).
   - **Tools → Photo resizer**: pick a photo, choose 100 KB, tap **Resize**, then **Save** (it goes to Pictures/Plainly).
   - **Tools → Loan & mortgage calculator**: change the numbers and watch the chart update. Tap **Currency** to change it.
   - **Saved**: your bookmarked guide is there. Turn on aeroplane mode and open it: it still reads.
   - **Settings** (gear icon on Today): try Dark mode.
   - **Today**: tap the "Latest" circle at the top for stories, drag the Money in 10 seconds slider, and flip a 30-second answer card.
   - **Tools**: search "passport", open the passport photo maker, pick a photo and download the result.
   - **Notifications**: tap **Turn on** on the "Never miss a new guide" card.
   - **Ads**: you'll see "Advertisement" banners marked **Test Ad** on Today, in articles and under the calculators.
     Save files in a tool twice and a full-screen test ad appears after the second save.
4. Before you install the Play Store version later, uninstall this test copy (the Play version replaces it).

## Step 2: Create a Google Play developer account (1-2 days, US$25 once)

1. Go to **https://play.google.com/console/signup** and sign in with the Google account you want to own the app.
2. Choose **Yourself** (a personal account).
3. Pay the one-time **US$25** registration fee.
4. Enter your legal name and address, and verify your **phone number** and **email**.
5. Complete **identity verification** by uploading a government photo ID. Google usually confirms within 1-2 days by email.
6. Google may also ask you to prove you have an Android phone, by installing the **Play Console** app on it and signing in.

## Step 2B: Set up AdMob and send me 3 IDs (20 minutes)

AdMob is Google's ad network for apps (the app version of AdSense) and pays out the same way.

1. Go to **https://admob.google.com** and sign in with the same Google account. If you already use AdSense for the
   blog, AdMob links to it and pays to the same bank account (Google pays monthly once you pass US$100).
2. **Apps → Add app → Android**. To "Is the app listed on a supported app store?" answer **No** for now. App name:
   **Plainly**. Click **Add app**. Copy the **App ID** (it looks like `ca-app-pub-1234567890123456~1234567890`).
3. **Ad units → Add ad unit → Banner**. Name it `Inline banner` and click **Create**. Copy its **ad unit ID** (it looks like
   `ca-app-pub-1234567890123456/1234567890`).
4. **Add ad unit → Interstitial**. Name it `After saving a file` and click **Create**. Copy its ad unit ID.
5. **Privacy & messaging → European regulations → Create message**. Select Plainly, add your privacy policy URL
   (`https://randomthoughtsever.blogspot.com/2021/06/privacy-policy.html`), keep the default options and click **Publish**.
   Then do the same under **US state regulations**. This is the consent form the app shows in those regions.
6. Send me the **App ID** and the **two ad unit IDs** in chat. They aren't secret (they're inside every copy of the app).
   I'll build the signed `plainly-1.1.0.aab` with them.
7. **Don't tap the ads in your own app** once it uses your real IDs. Google bans accounts for that. Test copies I build
   always show sample ads, which are safe to tap.
8. After the app is live on Google Play (Step 7): in AdMob, open **Apps → Plainly → App settings → Add app store
   details** and link the Play listing. AdMob then reviews the app (a few days). Until that's done, few ads are shown.

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
| **Ads** | **Yes, my app contains ads** |
| **Content rating** | Start the questionnaire. Enter your email and choose the category **"Reference, News, or Educational"**. Answer **No** to every question (violence, sexuality, language, drugs, gambling, user interaction, sharing location, digital purchases). Submit. You'll get a rating such as "Everyone" or "Rated for 3+". |
| **Target audience** | Tick **18 and over** only (the money and health guides are written for adults, and this avoids the extra rules for children's apps). |
| **News apps** | No |
| **Data safety** | See below |
| **Government apps** | No |
| **Financial features** | "My app doesn't provide any financial features" (the calculators only do arithmetic: no loans, payments, trading or banking) |
| **Health apps** | None of these |
| **Notifications** | Nothing to declare. The app asks people's permission before showing new-guide notifications. |
| **Advertising ID** | **Yes**. Purpose: **Advertising or marketing** (and **Analytics** if offered). |

**Data safety answers** (these cover what Google's ads software collects; the app itself collects nothing else):
1. "Does your app collect or share any of the required user data types?" **Yes**.
2. "Is all of the user data collected by your app encrypted in transit?" **Yes**.
3. "Which of the following methods of account creation does your app support?" **My app does not allow users to create an account**.
4. "Do you provide a way for users to request that their data is deleted?" **No** (you hold no user data; people
   reset their advertising ID in their phone's settings).
5. Tick these data types, and for each answer **Collected: Yes, Shared: Yes, Processed ephemerally: No,
   Required** (users can't turn it off inside the app), with the purposes listed:

| Data type (section → type) | Purposes to tick |
|---|---|
| **Location → Approximate location** | Advertising or marketing, Analytics, Fraud prevention, security and compliance |
| **App activity → App interactions** | Advertising or marketing, Analytics, Fraud prevention, security and compliance |
| **App info and performance → Crash logs** | Analytics, Fraud prevention, security and compliance |
| **App info and performance → Diagnostics** | Advertising or marketing, Analytics, Fraud prevention, security and compliance |
| **Device or other IDs → Device or other IDs** | Advertising or marketing, Analytics, Fraud prevention, security and compliance |

6. Click **Next**, check the summary and **Save**. This matches Google's own disclosure for its ads software and the
   app's privacy policy.

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
   - **Upload `plainly-1.1.0.aab`.**
   - Release name: `1.1.0`. Release notes: `First release of Plainly.`
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
