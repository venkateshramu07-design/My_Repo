# Google Play store listing: Plainly (global, English)

Paste these into **Grow users → Store presence → Main store listing**. Default language: **English (United States) – en-US**.
The listing is written for a worldwide audience: no country-specific exams or forms, and amounts in the app follow the
reader's own currency (16 currencies, chosen automatically from the phone's region).

How the text is optimised for Play Store search:
- The **title** and **short description** carry the most weight. The title keeps the brand line "Money, Mind & Work";
  the short description adds the highest-intent searches the app really serves (money guides, calculators, PDF and photo tools).
- The **full description** names each feature the way people search for it worldwide: *loan calculator*,
  *mortgage calculator*, *compound interest*, *investment calculator*, *FIRE calculator*, *credit card payoff*,
  *emergency fund*, *compress PDF*, *resize photo in KB*, *QR code generator*, *Wi-Fi QR code*, *budgeting*, *personal finance*.
  Each appears once or twice in natural sentences. Google Play penalises keyword lists and repetition.
- Google Play doesn't allow "free", "best", "#1", "top" or prices in the title or icon, so none are used.
- The app shows ads (Google AdMob), so the listing never says "no ads". Play Console marks the listing "Contains ads".

---

## App name (max 30 characters)

```
Plainly: Money, Mind & Work
```

## Short description (max 80 characters)

```
Money guides, 72 calculators and tools, PDF & photo tools. Fresh every day.
```

## Full description (max 4,000 characters)

```
Plainly gives you clear, practical guides on money, the mind and work, plus a set of calculators and file tools that run entirely on your phone. No account and no sign-up. Open it and start.

A NEW PAGE EVERY DAY
Today gives you a fresh pick every day, quick-answer stories you tap through, what was just published and flip cards that teach you something in 30 seconds. New guides appear the moment they're published, and you can get a notification for each one.

THE ANSWER FIRST
Every guide opens with a quick answer, so you get what you came for in seconds. Then come the details: real numbers, comparison tables and step-by-step instructions, laid out cleanly for a phone screen.

WHAT YOU CAN READ
• Personal finance: budgeting, debt, credit scores, bank accounts, student loans, car loans and taxes
• Investing: index funds, compound interest, retirement and financial independence (FIRE)
• Career and work: pay, interviews, burnout and job changes
• Mind and self-improvement: habits, focus, sleep and stress
• Living abroad: moving money between countries, travel rules and costs
New guides arrive every week.

MONEY CALCULATORS
• Loan and mortgage calculator: monthly payment, total interest and how much an extra payment each month saves
• Investment growth calculator: see compound interest at work with a monthly investment and a yearly raise
• Credit card payoff calculator: when you'll be debt-free, and the payment that clears it in two years
• FIRE calculator: your financial independence number and how many years until you reach it
• Emergency fund calculator: how many months of cover you have and how long until you're fully funded
Results update as you type, with clear charts. Choose your currency: US dollar, euro, pound, Canadian and Australian dollar, rupee, yen and more.

72 TOOLS IN ONE APP
• Photo resizer: shrink any photo to an exact size in KB for online forms, several photos at once
• PDF compressor: make a PDF small enough for an upload limit
• QR code generator: links, text and Wi-Fi QR codes that never expire
• Passport and visa photo maker with official sizes for 8 countries
• PDF toolkit: merge, split, reorder and sign PDFs; turn photos into a PDF
• Resume builder and invoice generator with free PDF downloads
• Currency converter, inflation calculator and debt payoff planner
• Rent or buy, car affordability, life insurance and 401(k) calculators
• Meeting time zone planner, password generator and word counter
• Sleep cycle calculator, habit tracker and more
Photos and PDFs are processed on your device and never uploaded. Save to your phone or share straight to any app.

EXPLORE AND SWIPE
Browse collections like "Get out of debt" and "Invest and retire early", money facts for 35 countries, every topic, or all guides from A to Z. Or swipe through quick answers in Reads: one guide per screen, its key point up front.

MADE FOR READING
• Save any guide to read later, even offline
• Pick up where you left off with your reading history
• Adjust the text size, and switch between light and dark mode
• Search every guide, or browse by topic

PRIVATE BY DESIGN
Plainly has no accounts. It doesn't ask for your location, contacts, camera or storage permission, and your photos, PDFs, saved guides and settings stay on your phone. The app is free and supported by a few clearly labelled ads; where the law requires it, you're asked for consent first.

Plainly is the app for the Blog Digest guides. The calculators give estimates for planning, not financial advice.
```

---

## Graphics (upload in this order)

| Item | File |
|---|---|
| App icon (512 × 512) | `store/play-icon-512.png` |
| Feature graphic (1024 × 500) | `store/feature-graphic-1024x500.png` |
| Phone screenshots (1080 × 1920, 8 of them) | `store/screenshots/screenshot-1.png` … `screenshot-8.png` |

The screenshots are real screens of the app, rendered from the app's own code with live articles from the blog
(`ScreenshotTest` in `app/src/test`, framed by `store/make_screenshots.py`).

1. Your day, plainly explained (Today)
2. Explore 295 guides your way (Explore)
3. Swipe through quick answers (Reads)
4. 72 tools in your pocket (Tools)
5. Stories that teach you something (stories)
6. See your money grow as you drag (Today's money widget)
7. The answer first. Then the detail. (article)
8. Your streak, your library (You)

## Store settings

- **App category:** Finance
- **Tags** (pick up to 5 that Play Console offers): Personal finance, Calculators, Budgeting, Investing, Productivity
- **Contact email:** your email address (shown publicly on the listing)
- **Website:** https://randomthoughtsever.blogspot.com
