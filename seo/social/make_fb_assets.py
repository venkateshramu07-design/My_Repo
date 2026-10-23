# Draw the Facebook Page cover (1640x624; phones crop the sides, so text stays in the middle 1100px).
#   python3 seo/social/make_fb_assets.py <path to Fraunces variable .ttf>
import os, sys
from PIL import Image, ImageDraw, ImageFilter, ImageFont
HERE = os.path.dirname(os.path.abspath(__file__))
SANS = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
W, H = 1640, 624
def serif(size):
    f = ImageFont.truetype(sys.argv[1], size); f.set_variation_by_axes([144, 800, 0, 0]); return f
deep, a = (18, 20, 27), (226, 70, 44)
grad = Image.new("L", (W, H))
grad.putdata([min(255, int(255 * ((x / W) * .6 + (y / H) * .4) ** 1.4)) for y in range(H) for x in range(W)])
img = Image.composite(Image.new("RGB", (W, H), deep), Image.new("RGB", (W, H), a), grad)
rings = img.copy(); dr = ImageDraw.Draw(rings)
for r in (420, 320, 220):
    dr.ellipse((1480 - r, 560 - r, 1480 + r, 560 + r), outline=(255, 255, 255), width=2)
img = Image.blend(img, rings, .16)
d = ImageDraw.Draw(img)
cx = W // 2
d.text((cx, 190), "BLOG DIGEST", font=ImageFont.truetype(SANS, 30), fill=(255, 226, 214), anchor="mm", features=["lnum"])
d.text((cx, 290), "Practical guides to money,", font=serif(84), fill=(255, 255, 255), anchor="mm", features=["lnum"])
d.text((cx, 390), "mind and work", font=serif(84), fill=(255, 255, 255), anchor="mm", features=["lnum"])
d.text((cx, 478), "Personal finance · Economics · Careers · Psychology · Well-being", font=ImageFont.truetype(SANS, 26), fill=(240, 232, 228), anchor="mm")
img.save(os.path.join(HERE, "fb-cover.jpg"), quality=88, optimize=True)
print("wrote fb-cover.jpg")
