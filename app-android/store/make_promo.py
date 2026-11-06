"""Makes a ~35-second landscape promo video (1920x1080, for YouTube / the Play listing) from the framed Play Store
screenshots: an intro card, one scene per screenshot with its caption, and an end card. Needs Pillow and ffmpeg."""
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import os, subprocess, shutil, tempfile
HERE = os.path.dirname(os.path.abspath(__file__))
SHOTS = os.path.join(HERE, '..', 'app', 'build', 'shots')
FONT = os.path.join(HERE, '..', 'app', 'src', 'main', 'res', 'font')
OUT = os.path.join(HERE, 'promo-video.mp4')
W, H, FPS = 1920, 1080, 30
DEEP, GOLD = (10, 27, 47), (226, 178, 98)

def font(name, size): return ImageFont.truetype(os.path.join(FONT, name), size)

def background(top=(18, 48, 79), bottom=DEEP):
    g = Image.new('RGB', (W, H)); d = ImageDraw.Draw(g)
    for y in range(H):
        t = y / H; d.line([(0, y), (W, y)], fill=tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3)))
    glow = Image.new('L', (W, H), 0); ImageDraw.Draw(glow).ellipse((1000, 80, 1800, 1100), fill=90)
    g.paste(GOLD, (0, 0), glow.filter(ImageFilter.GaussianBlur(180)))
    return g

def phone(shot, height=900):
    scr = Image.open(os.path.join(SHOTS, shot + '.png')).convert('RGB')
    sh = height - 36; sw = int(scr.width * sh / scr.height)
    scr = scr.resize((sw, sh), Image.LANCZOS)
    frame = Image.new('RGBA', (sw + 36, sh + 36), (0, 0, 0, 0))
    ImageDraw.Draw(frame).rounded_rectangle((0, 0, sw + 35, sh + 35), 70, fill=(14, 20, 28, 255))
    mask = Image.new('L', (sw, sh), 0); ImageDraw.Draw(mask).rounded_rectangle((0, 0, sw, sh), 54, fill=255)
    frame.paste(scr, (18, 18), mask)
    return frame

def card(title, sub, shot=None, kicker='PLAINLY'):
    bg = background(); d = ImageDraw.Draw(bg)
    x = 140 if shot else 0
    if shot:
        p = phone(shot)
        shadow = Image.new('L', (W, H), 0); ImageDraw.Draw(shadow).rounded_rectangle((1150, 110, 1150 + p.width, 110 + p.height), 80, fill=140)
        bg.paste((0, 0, 0), (0, 0), shadow.filter(ImageFilter.GaussianBlur(40)))
        bg.paste(p, (1120, 90), p)
        d = ImageDraw.Draw(bg)
        d.rounded_rectangle((x, 300, x + 70, 312), 6, fill=GOLD)
        d.text((x, 340), kicker, font=font('jakarta_bold.ttf', 36), fill=GOLD)
        d.multiline_text((x, 400), title, font=font('fraunces_bold.ttf', 104), fill=(255, 255, 255), spacing=10)
        d.text((x + 4, 680), sub, font=font('jakarta_medium.ttf', 44), fill=(196, 210, 226))
    else:
        tw = d.multiline_textbbox((0, 0), title, font=font('fraunces_bold.ttf', 120), align='center')
        d.multiline_text(((W - (tw[2] - tw[0])) // 2, 360), title, font=font('fraunces_bold.ttf', 120), fill=(255, 255, 255), align='center', spacing=10)
        sb = d.textbbox((0, 0), sub, font=font('jakarta_medium.ttf', 48))
        d.text(((W - (sb[2] - sb[0])) // 2, 680), sub, font=font('jakarta_medium.ttf', 48), fill=GOLD)
        icon = os.path.join(HERE, 'play-icon-512.png')
        if os.path.exists(icon):
            ic = Image.open(icon).convert('RGBA').resize((200, 200))
            m = Image.new('L', (200, 200), 0); ImageDraw.Draw(m).rounded_rectangle((0, 0, 199, 199), 46, fill=255)
            bg.paste(ic, ((W - 200) // 2, 110), m)
    return bg

SCENES = [
    (None, 'Plainly\nMoney, Mind & Work', 'Clear answers. Five minutes a day.', None),
    ('60-today-daily-run', 'Your 5-minute\nDaily Run', 'A story, a quiz, a puzzle. Done.', 'EVERY DAY'),
    ('64-daily-puzzle', 'A new puzzle\nevery day', 'The same for everyone. Share your score.', 'PLAY'),
    ('66-sim-month', 'Live a year\nof money', 'Your choices. Real lessons.', 'SIMULATOR'),
    ('25-check-result', 'Your money\nscore', 'In 60 seconds, with what to fix first.', 'CHECK-UP'),
    ('72-translate-picker', 'In your\nlanguage', '26 languages, on your phone.', 'TRANSLATE'),
    ('68-rewards', 'Level up as\nyou learn', 'XP, streaks, cards and rewards.', 'PROGRESS'),
    ('05-tools', '72 tools in\nyour pocket', 'Photos, PDFs, money and more.', 'TOOLS'),
    ('69-calm', 'Breathe.\nFocus. Sleep.', 'A calm corner, when you need it.', 'CALM'),
    (None, 'Get Plainly', 'On Google Play', None),
]

def main():
    if not shutil.which('ffmpeg'): raise SystemExit('ffmpeg is needed')
    tmp = tempfile.mkdtemp()
    files = []
    for i, (shot, title, sub, kicker) in enumerate(SCENES):
        f = os.path.join(tmp, f'{i:02d}.png'); card(title, sub, shot, kicker or 'PLAINLY').save(f); files.append(f)
    dur, fade = 3.6, 0.5
    args = ['ffmpeg', '-y', '-loglevel', 'error']
    for f in files: args += ['-i', f]   # one still each; zoompan turns it into a clip of dur seconds
    # gentle zoom on each scene, then crossfades between them
    chains = [f'[{i}:v]scale={W*1.06:.0f}:-1,zoompan=z=\'min(zoom+0.0006,1.05)\':d={int(dur*FPS)}:s={W}x{H}:fps={FPS},format=yuv420p[v{i}]' for i in range(len(files))]
    prev, offset = 'v0', 0.0
    for i in range(1, len(files)):
        offset += dur - fade
        chains.append(f'[{prev}][v{i}]xfade=transition=fade:duration={fade}:offset={offset:.2f}[x{i}]'); prev = f'x{i}'
    args += ['-filter_complex', ';'.join(chains), '-map', f'[{prev}]', '-c:v', 'libx264', '-preset', 'medium', '-crf', '20', '-pix_fmt', 'yuv420p',
             '-movflags', '+faststart', OUT]
    subprocess.run(args, check=True)
    print('wrote', OUT)

if __name__ == '__main__': main()
