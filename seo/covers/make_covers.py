# Draw 1200x630 share/cover images for posts published from seo/hubs and seo/articles, which have no image of
# their own (so WhatsApp, Facebook and X previews show none).
#   python3 seo/covers/make_covers.py <path to Fraunces variable .ttf>
import os, sys
from PIL import Image, ImageDraw, ImageFilter, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
SANS = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
POSTS = {  # name: (eyebrow, title, accent colour)
    "money-start-here": ("START HERE · MONEY", "Personal Finance for Beginners: A Step-by-Step Guide", "#e2462c"),
    "mind-start-here": ("START HERE · MIND", "Mental Well-Being and Self-Improvement: A Practical Guide", "#6b5cff"),
    "career-start-here": ("START HERE · CAREER", "Career Guide: Work Smarter, Avoid Burnout and Grow", "#2f7de1"),
    "emergency-fund-guide": ("PERSONAL FINANCE · CALCULATOR", "How Much Emergency Fund Do You Need?", "#0f9d76"),
    "good-credit-score-cibil": ("PERSONAL FINANCE", "What Is a Good CIBIL Score? How to Check and Improve It", "#e48b12"),
    "prepay-home-loan-or-invest": ("PERSONAL FINANCE", "Prepay Home Loan or Invest? How to Decide", "#0e9bb5"),
    "gdp-measuring-economy": ("ECONOMICS", "What Is GDP? Its Role in Measuring an Economy", "#7c5cff"),
    "bitcoin-losing-its-hype": ("CRYPTOCURRENCY", "Is Bitcoin Losing Its Hype? What the Data Says", "#e48b12"),
    "home": ("BLOG DIGEST", "Practical guides to money, mind and work", "#e2462c"),  # homepage share image
}
W, H = 1200, 630


def rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (1, 3, 5))


def serif(size, weight):
    f = ImageFont.truetype(sys.argv[1], size)
    f.set_variation_by_axes([144, weight, 0, 0])
    return f


def wrap(draw, text, font, width):
    lines, line = [], ""
    for word in text.split():
        test = (line + " " + word).strip()
        if draw.textlength(test, font=font) <= width or not line:
            line = test
        else:
            lines.append(line); line = word
    return lines + [line]


def cover(eyebrow, title, accent):
    a, deep = rgb(accent), (18, 20, 27)
    img = Image.new("RGB", (W, H), deep)
    # diagonal gradient from the accent to near-black, plus a soft glow
    # mask runs 0 (accent, top left) to 255 (near-black, bottom right), eased so the accent dominates the text side
    grad = Image.new("L", (W, H))
    grad.putdata([min(255, int(255 * ((x / W) * .55 + (y / H) * .45) ** 1.6)) for y in range(H) for x in range(W)])
    img = Image.composite(Image.new("RGB", (W, H), deep), Image.new("RGB", (W, H), a), grad)
    glow = Image.new("RGB", (W, H), (0, 0, 0)); ImageDraw.Draw(glow).ellipse((760, -260, 1400, 380), fill=(255, 255, 255))
    img = Image.blend(img, Image.composite(Image.new("RGB", (W, H), (255, 255, 255)), img, glow.convert("L").filter(ImageFilter.GaussianBlur(120))), .22)
    rings = img.copy(); dr = ImageDraw.Draw(rings)
    for r in (300, 220, 140):  # faint rings, lower right
        dr.ellipse((1060 - r, 560 - r, 1060 + r, 560 + r), outline=(255, 255, 255), width=2)
    img = Image.blend(img, rings, .18)
    d = ImageDraw.Draw(img)
    d.text((72, 70), eyebrow, font=ImageFont.truetype(SANS, 24), fill=(255, 225, 214))
    size = 76
    while True:
        f = serif(size, 800); lines = wrap(d, title, f, 900)
        if len(lines) <= 3 or size <= 52: break
        size -= 4
    y = 130
    for ln in lines:
        d.text((72, y), ln, font=f, fill=(255, 255, 255)); y += int(size * 1.12)
    # brand badge
    d.rounded_rectangle((72, 520, 124, 572), radius=14, fill=(242, 130, 60))
    d.text((98, 546), "B", font=serif(36, 800), fill=(255, 255, 255), anchor="mm")
    d.text((140, 546), "Blog Digest", font=serif(32, 800), fill=(255, 255, 255), anchor="lm")
    return img


for name, (eyebrow, title, accent) in POSTS.items():
    cover(eyebrow, title, accent).convert("RGB").save(os.path.join(HERE, name + ".jpg"), quality=84, optimize=True, progressive=True)
    print("wrote", name + ".jpg")
