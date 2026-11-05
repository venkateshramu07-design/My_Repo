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
Your money score, a plan made for you, 72 free tools and a friendly community.
```

## Full description (max 4,000 characters)

```
Plainly gives you clear, practical guides on money, the mind and work, plus a set of calculators and file tools that run entirely on your phone. No account and no sign-up. Open it and start.

MADE FOR YOU IN 20 SECONDS
Tell Plainly what you want help with: paying off debt, saving and investing, a calmer mind, your career or living abroad. You get a plan with three guides and the tools to act on them. Take the 60-second Money Health Check for your score out of 100 and the one thing to fix first, then share it.

YOUR NUMBERS, YOUR HABITS
Add your pay and spending (kept on your phone) to see your FIRE number, safety net, savings rate and debt-free date. Join a challenge like the 7-day no-spend or the 30-day debt sprint, keep your streak and earn badges.

YOUR 5-MINUTE DAILY RUN
A story, a quiz, one small action, the daily puzzle and a mood check-in, then you're done for the day. Earn XP, level up, unlock colour themes and collect a card for every guide you finish.

PLAY AND UNWIND
A new daily puzzle (the same for everyone; share your result), A Year of Money (live 12 months of money decisions in five minutes), a Calm corner with breathing, a focus timer and sleep sounds, plus Rock Paper Scissors, Money Match, Number Rush, Tic-Tac-Toe and Reaction Time. Offline.

COMMUNITY
Read what other readers say about every guide, join the conversation, and see the most active voices. Reading needs no account; you post with your Google account, like any comment on the blog.

A NEW PAGE EVERY DAY
A fresh pick every day, quick-answer stories, flip cards and quizzes. New guides appear the moment they're published.

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
Loan and mortgage, investment growth, credit card payoff, FIRE and emergency fund calculators. Results update as you type, with clear charts. Choose your currency: US dollar, euro, pound, Canadian and Australian dollar, rupee, yen and more.

72 TOOLS IN ONE APP
• Photo resizer: shrink any photo to an exact size in KB for online forms, several photos at once
• PDF compressor: make a PDF small enough for an upload limit
• QR code generator: links, text and Wi-Fi QR codes that never expire
• Passport and visa photo maker with official sizes for 8 countries
• PDF toolkit: merge, split, reorder and sign PDFs; turn photos into a PDF
• Resume builder and invoice generator with free PDF downloads
• Currency converter, inflation calculator and debt planner
• Time zone planner, password generator and more
Photos and PDFs are processed on your device and never uploaded. Save to your phone or share straight to any app.

EXPLORE AND SWIPE
Collections like "Get out of debt", money facts for 35 countries, every topic, or swipe through quick answers one screen at a time.

MADE FOR READING
Save guides for offline reading, quick checks inside every guide, a notebook for your favourite lines, text size and dark mode.

PRIVATE BY DESIGN
No accounts, and no location, contacts, camera or storage permission. Your photos, PDFs, numbers and settings stay on your phone. The app is free, supported by a few labelled ads; where the law requires it, you're asked for consent first.

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
