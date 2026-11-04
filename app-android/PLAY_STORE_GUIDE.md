# How to put Plainly on Google Play: step by step

You don't need to know app development. Everything technical is done. What's left are the steps only the account
owner can do in Google Play Console. Allow about 3 weeks in total, mostly waiting: about 2 days for account checks,
the 14-day test that Google requires for new accounts, then about a week for review.

## What you have

| File | What it's for |
|---|---|
| `app-release.aab` | **The file you upload to Google Play.** |
| `app-release.apk` | To try the app on your own Android phone first. Don't upload this one. |
| `plainly-upload.jks` + `SIGNING-KEY.txt` | Your private upload key and its password. **Keep both safe** (for example in Google Drive and on a USB stick). Every future update must be signed with this key. Never share them publicly. |
| `store/` folder | Icon, feature graphic, 4 screenshots and all the listing text (`store/listing.md`). |

App details: name **Plainly** (store title "Plainly: Money, Mind & Work"), package `com.randomthoughtsever.plainly`, version 1.0.0 (code 1),
targets Android 16 (API 36) as Google Play currently requires, and works on Android 10 and newer.

---

## Who can use the app

Anyone. There is no account, password or sign-up: people install Plainly and it opens straight to your guides and
tools. The upload key in this folder is only for you as the publisher (Google uses it to check that updates really
come from you); users never see it. The one wait is Google's rule for new developer accounts (Step 6): a 14-day test
with 12 people before the app is public. After that, anyone can install and use it immediately.

## Step 1: Try the app on your phone (10 minutes)

1. Send `app-release.apk` to your Android phone (email it to yourself, or put it in Google Drive) and tap it.
2. Android will say it's blocked for security. Tap **Settings**, turn on **Allow from this source**, go back and tap **Install**.
3. Open **Plainly** and check:
   - The tabs at the bottom (Home, Money, Mind, Career, Tools) open the right sections.
   - In **Tools**, open "Compress PDF" or "Resize image", pick a file, then tap **Download**. A message
     "Saved to Downloads" appears; tap **Open** to see the file.
   - Pull down at the top of a page to refresh. Turn on aeroplane mode and reopen the app: you'll see the offline screen.
4. When you install the Play Store version later, uninstall this test copy first.

## Step 2: Create a Google Play developer account (about 1-2 days, $25 once)

1. Go to **https://play.google.com/console/signup** and sign in with the Google account you want to own the app.
2. Choose **Yourself** (a personal account).
3. Pay the one-time **US$25** registration fee.
4. Fill in your legal name and address, and verify your **phone number** and **email**.
5. Complete **identity verification**: upload a government ID (passport, driving licence, Aadhaar or PAN). Google
   usually confirms within 1-2 days by email.
6. Google may also ask you to verify that you have an Android phone, by signing in to the **Play Console app** on it.

## Step 3: Create the app (5 minutes)

In Play Console, click **Create app** and enter:
- App name: **Plainly: Money, Mind & Work**
- Default language: **English (United States) – en-US**
- App or game: **App**
- Free or paid: **Free**
- Tick the two declarations (Developer Program Policies and US export laws) and click **Create app**.

## Step 4: Fill in "Set up your app" (about 30 minutes)

On the app's **Dashboard**, open each task under **Set up your app** and answer as follows.

| Task | What to enter |
|---|---|
| **Privacy policy** | `https://randomthoughtsever.blogspot.com/2021/06/privacy-policy.html` (it now has a section on the app) |
| **App access** | All functionality is available without special access |
| **Ads** | No, my app does not contain ads (the app blocks the website's ads) |
| **Content rating** | Start the questionnaire. Enter your email, choose category **"Reference, News, or Educational"**. Answer **No** to violence, sexuality, language, controlled substances and gambling. To "Can users interact or exchange content?", answer **Yes** if blog comments are switched on, otherwise **No**. Submit; you'll get a rating such as "Everyone" or "Teen". |
| **Target audience** | Choose **18 and over** only (the money and health guides are written for adults; this also avoids the extra rules for children's apps). |
| **News apps** | No |
| **Data safety** | See below |
| **Government apps** | No |
| **Financial features** | "My app doesn't provide any financial features" (the calculators only do arithmetic; no loans, payments or banking) |
| **Health apps** | None of these |
| **Advertising ID** | No (the app doesn't use it) |

**Data safety answers** (these describe what the website can collect inside the app):
1. Does your app collect or share any of the required user data types? **Yes**
2. Is all user data encrypted in transit? **Yes** (the blog only uses https)
3. Do you provide a way for users to request that their data be deleted? **Yes**. Users can unsubscribe from emails
   or contact you through the blog's Contact page.
4. Data types: under **Personal info**, tick **Email address**. For it, answer:
   - Collected: **Yes**. Shared: **No**.
   - Processed ephemerally: **No**.
   - Required or optional: **Optional** (only if someone subscribes to the newsletter or leaves a comment).
   - Purpose: **Developer communications**.
5. Save and submit. Nothing else is collected by the app: no location, no contacts, no photos uploaded, no device IDs.

## Step 5: Store listing (15 minutes)

Go to **Grow users → Store presence → Main store listing** and copy the text from `store/listing.md`:
- Short description and full description.
- Upload the **App icon** (`store/play-icon-512.png`), the **Feature graphic** (`store/feature-graphic-1024x500.png`) and
  the 4 **Phone screenshots** (`store/screenshot-*.png`).
- Then go to **Store settings**: category **News & Magazines**, and your contact email.
- Click **Save**.

## Step 6: Closed test with 12 testers for 14 days (required for new personal accounts)

Google requires every new personal developer account to test with **at least 12 people for 14 days in a row** before
the app can go public.

1. Go to **Test and release → Testing → Closed testing** and click **Create track**, or use the default "Closed testing - Alpha".
2. Under **Testers**, create an email list, add the Gmail addresses of **at least 12 people** (friends and family with
   Android phones), and save. (Adding a few more than 12 is wise, in case someone drops out.)
3. Click **Create release**.
   - When asked about app signing, keep **"Use Google-generated key" (Play App Signing)**, the recommended default.
   - **Upload `app-release.aab`.**
   - Release name: `1.0.0`. Release notes: `First release of Plainly.`
4. Click **Next → Save → Send for review**. The first review usually takes a few hours to a few days.
5. When it's approved, copy the **opt-in link** from the Testers tab and send it to your testers. Each tester must:
   open the link, tap **Become a tester**, then **install the app from Google Play** and keep it installed.
6. Wait **14 days**, with at least 12 testers opted in throughout. Ask them to open the app a few times.

## Step 7: Go public (production)

1. After 14 days, the Dashboard shows **Apply for production**. Click it and answer the short questions about your
   test, for example: "Friends and family tested reading articles, the tools and offline mode; no problems found".
2. When Google approves (often within 7 days), go to **Test and release → Production → Create release**, choose the
   same build from your library (or upload `app-release.aab` again), add release notes, click
   **Next → Save → Send for review**, then **Start rollout to Production**.
3. After the final review, Plainly appears on Google Play. Your listing link will be
   `https://play.google.com/store/apps/details?id=com.randomthoughtsever.plainly`

## If Google rejects the app

The most common reason for website-based apps is "minimum functionality" (the app is "just a website"). This app adds
native tabs, file saving for the tools, offline reading, pull-to-refresh, a splash screen and no ads, which normally
satisfies the policy. If you still get a rejection email, send me the text and I'll make the changes and build a new version.

## Updating the app later

Ask me for an update. I'll change the app, increase the version number and build a new `.aab` signed with your key.
You then upload it under **Production → Create release**. Keep `plainly-upload.jks` and its password safe: if
they're lost, you can ask Google Play support to reset the upload key, but that takes several days.
