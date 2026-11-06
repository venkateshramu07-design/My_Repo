"""Frames the real app screens (rendered by ScreenshotTest into app/build/shots) as 1080x1920 Play Store screenshots."""
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import os
HERE = os.path.dirname(os.path.abspath(__file__))
SHOTS = os.path.join(HERE, '..', 'app', 'build', 'shots')
FONT = os.path.join(HERE, '..', 'app', 'src', 'main', 'res', 'font')
OUT = os.path.join(HERE, 'screenshots')
W, H = 1080, 1920
NAVY, DEEP, GOLD, PAGE = (31, 78, 121), (10, 27, 47), (226, 178, 98), (246, 244, 240)

SLIDES = [
    ('60-today-daily-run', 'Five minutes a day.\nThen you\'re done.', 'A story, a quiz, one small step, a puzzle.', 'dark'),
    ('64-daily-puzzle', 'A new puzzle\nevery day', 'The same for everyone. Share your score.', 'light'),
    ('66-sim-month', 'Live a whole year\nof money', 'Your choices, real lessons, a final score.', 'dark'),
    ('25-check-result', 'Your money score\nin 60 seconds', 'And the one thing to fix first.', 'light'),
    ('72-translate-picker', 'Read it in\nyour language', '26 languages, translated on your phone.', 'dark'),
    ('68-rewards', 'Level up as\nyou learn', 'XP, streaks, guide cards and rewards.', 'light'),
    ('05-tools', '72 tools\nin your pocket', 'Photos, PDFs, money, work and travel.', 'dark'),
    ('69-calm', 'Breathe. Focus.\nSleep.', 'A calm corner with soothing sounds.', 'light'),
]

def font(name, size): return ImageFont.truetype(os.path.join(FONT, name), size)

def gradient(top, bottom):
    g = Image.new('RGB', (W, H))
    d = ImageDraw.Draw(g)
    for y in range(H):
        t = y / H
        d.line([(0, y), (W, y)], fill=tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3)))
    return g

os.makedirs(OUT, exist_ok=True)
for n, (shot, title, sub, tone) in enumerate(SLIDES, 1):
    dark = tone == 'dark'
    bg = gradient((18, 48, 79), DEEP) if dark else gradient((252, 250, 246), (234, 228, 216))
    d = ImageDraw.Draw(bg)
    ink = (255, 255, 255) if dark else DEEP
    soft = (196, 210, 226) if dark else (91, 101, 118)
    # soft glow behind the phone
    glow = Image.new('L', (W, H), 0); ImageDraw.Draw(glow).ellipse((90, 640, 990, 1900), fill=90 if dark else 60)
    bg.paste(GOLD if dark else NAVY, (0, 0), glow.filter(ImageFilter.GaussianBlur(160)))
    d = ImageDraw.Draw(bg)
    d.rounded_rectangle((90, 112, 150, 122), 5, fill=GOLD)
    d.multiline_text((90, 150), title, font=font('fraunces_bold.ttf', 84), fill=ink, spacing=8)
    d.text((92, 365), sub, font=font('jakarta_medium.ttf', 36), fill=soft)
    # phone: real screen inside a dark rounded frame
    scr = Image.open(os.path.join(SHOTS, shot + '.png')).convert('RGB')
    sw = 760; sh = int(scr.height * sw / scr.width)
    scr = scr.resize((sw, sh), Image.LANCZOS)
    pad = 22; fx = (W - sw) // 2 - pad; fy = 470
    fw, fh = sw + pad * 2, sh + pad * 2
    shadow = Image.new('L', (W, H), 0); ImageDraw.Draw(shadow).rounded_rectangle((fx + 10, fy + 30, fx + fw + 10, fy + fh + 30), 90, fill=150)
    bg.paste((0, 0, 0), (0, 0), shadow.filter(ImageFilter.GaussianBlur(40)))
    d = ImageDraw.Draw(bg)
    d.rounded_rectangle((fx, fy, fx + fw, fy + fh), 92, fill=(14, 20, 28))
    mask = Image.new('L', (sw, sh), 0); ImageDraw.Draw(mask).rounded_rectangle((0, 0, sw, sh), 72, fill=255)
    bg.paste(scr, (fx + pad, fy + pad), mask)
    bg.save(os.path.join(OUT, f'screenshot-{n}.png'), optimize=True)
    print('wrote', n, shot)
