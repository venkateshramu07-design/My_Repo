# Draw 1000x1500 Pinterest pins (also used as Facebook images) for the articles in pack.json, in the same style
# as the covers in seo/covers/.
#   python3 seo/social/make_pins.py <path to Fraunces variable .ttf>
import json, os, sys
from PIL import Image, ImageDraw, ImageFilter, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
SANS = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
SANS_REG = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
W, H, M = 1000, 1500, 84
DEEP, CREAM, INK = (18, 20, 27), (250, 246, 239), (21, 23, 29)


def txt(d, xy, text, font, **kw):  # lining figures: Fraunces' default old-style 3 reads like a 5 at this size
    d.text(xy, text, font=font, features=["lnum"], **kw)


def rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (1, 3, 5))


def serif(size, weight=800):
    f = ImageFont.truetype(sys.argv[1], size)
    f.set_variation_by_axes([144, weight, 0, 0])
    return f


def wrap(draw, text, font, width):
    lines, line = [], ""
    for word in text.split():
        test = (line + " " + word).strip()
        if draw.textlength(test, font) <= width or not line:
            line = test
        else:
            lines.append(line); line = word
    return lines + [line]


def fit(draw, text, width, start, low, max_lines):
    size = start
    while True:
        f = serif(size); lines = wrap(draw, text, f, width)
        if len(lines) <= max_lines or size <= low:
            return f, size, lines
        size -= 4


def gradient(accent):
    a = rgb(accent)
    grad = Image.new("L", (W, H))
    grad.putdata([min(255, int(255 * ((x / W) * .35 + (y / H) * .65) ** 1.5)) for y in range(H) for x in range(W)])
    img = Image.composite(Image.new("RGB", (W, H), DEEP), Image.new("RGB", (W, H), a), grad)
    glow = Image.new("L", (W, H)); ImageDraw.Draw(glow).ellipse((520, -320, 1300, 420), fill=255)
    img = Image.composite(Image.new("RGB", (W, H), (255, 255, 255)), img, glow.filter(ImageFilter.GaussianBlur(150)).point(lambda v: v * .25))
    rings = img.copy(); dr = ImageDraw.Draw(rings)
    for r in (360, 270, 180):
        dr.ellipse((880 - r, 1330 - r, 880 + r, 1330 + r), outline=(255, 255, 255), width=2)
    return Image.blend(img, rings, .16)


def brand(d, y, color=(255, 255, 255), url_color=(255, 255, 255)):
    d.rounded_rectangle((M, y, M + 60, y + 60), radius=16, fill=(242, 130, 60))
    txt(d, (M + 30, y + 30), "B", serif(42), fill=(255, 255, 255), anchor="mm")
    txt(d, (M + 78, y + 30), "Blog Digest", serif(38), fill=color, anchor="lm")
    txt(d, (W - M, y + 30), "randomthoughtsever.blogspot.com", ImageFont.truetype(SANS_REG, 22), fill=url_color, anchor="rm")


def headline(pin, accent):
    img = gradient(accent); d = ImageDraw.Draw(img)
    txt(d, (M, 150), pin["eyebrow"], ImageFont.truetype(SANS, 30), fill=(255, 228, 216))
    f, size, lines = fit(d, pin["title"], W - 2 * M, 120, 80, 5)
    y = 220
    for ln in lines:
        txt(d, (M, y), ln, f, fill=(255, 255, 255)); y += int(size * 1.1)
    y += 40
    d.rectangle((M, y, M + 90, y + 8), fill=(255, 255, 255)); y += 50
    for ln in wrap(d, pin["sub"], ImageFont.truetype(SANS_REG, 40), W - 2 * M):
        txt(d, (M, y), ln, ImageFont.truetype(SANS_REG, 40), fill=(240, 236, 255)); y += 56
    by = min(1180, y + 70)
    d.rounded_rectangle((M, by, M + 400, by + 90), radius=45, fill=(255, 255, 255))
    txt(d, (M + 200, by + 45), "Read the guide  →", ImageFont.truetype(SANS, 32), fill=INK, anchor="mm")
    brand(d, 1360)
    return img


def listing(pin, accent):
    a = rgb(accent)
    img = Image.new("RGB", (W, H), CREAM); d = ImageDraw.Draw(img)
    f, size, lines = fit(d, pin["title"], W - 2 * M, 104, 70, 3)
    band = 170 + len(lines) * int(size * 1.08) + 70
    d.rectangle((0, 0, W, band), fill=a)
    txt(d, (M, 110), pin["eyebrow"], ImageFont.truetype(SANS, 30), fill=(255, 236, 228))
    y = 170
    for ln in lines:
        txt(d, (M, y), ln, f, fill=(255, 255, 255)); y += int(size * 1.08)
    items = pin["items"]
    step = min(150, int((1250 - band - 70) / len(items)))
    y = band + 70
    body = ImageFont.truetype(SANS, 40)
    for i, it in enumerate(items, 1):
        d.ellipse((M, y, M + 76, y + 76), fill=a)
        txt(d, (M + 38, y + 38), str(i), ImageFont.truetype(SANS, 40), fill=(255, 255, 255), anchor="mm")
        tl = wrap(d, it, body, W - 2 * M - 110)
        ty = y + 38 - (len(tl) - 1) * 25
        for ln in tl:
            txt(d, (M + 110, ty), ln, body, fill=INK, anchor="lm"); ty += 50
        y += step
    txt(d, (M, 1290), pin["sub"], ImageFont.truetype(SANS_REG, 34), fill=(93, 98, 113))
    d.line((M, 1345, W - M, 1345), fill=(225, 219, 208), width=2)
    brand(d, 1380, color=INK, url_color=(93, 98, 113))
    return img


def stat(pin, accent):
    a = rgb(accent)
    img = Image.new("RGB", (W, H), DEEP); d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 24, H), fill=a)
    f, size, lines = fit(d, pin["title"], W - 2 * M, 116, 72, 6)
    sf = ImageFont.truetype(SANS_REG, 38)
    subl = wrap(d, pin["sub"], sf, W - 2 * M)
    block = 310 + len(lines) * int(size * 1.14) + 50 + len(subl) * 54
    y = max(120, (1320 - block) // 2)  # centre the quote between the top and the brand row
    txt(d, (M, y), pin["eyebrow"], ImageFont.truetype(SANS, 30), fill=a)
    txt(d, (M - 10, y + 30), "“", serif(260), fill=a)
    y += 310
    for ln in lines:
        txt(d, (M, y), ln, f, fill=(255, 255, 255)); y += int(size * 1.14)
    y += 50
    for ln in subl:
        txt(d, (M, y), ln, sf, fill=(190, 195, 208)); y += 54
    brand(d, 1360, url_color=(160, 166, 180))
    return img


STYLES = {"headline": headline, "list": listing, "stat": stat}
pack = json.load(open(os.path.join(HERE, "pack.json"), encoding="utf-8"))
os.makedirs(os.path.join(HERE, "pins"), exist_ok=True)
for art in pack["articles"]:
    for n, pin in enumerate(art["pins"], 1):
        name = "%s-pin-%d.jpg" % (art["id"], n)
        STYLES[pin["style"]](pin, art["accent"]).save(os.path.join(HERE, "pins", name), quality=86, optimize=True, progressive=True)
        print("wrote", name)
