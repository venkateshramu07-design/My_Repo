# A fresh Google access token from the long-lived refresh token in the environment (GOOGLE_CLIENT_ID,
# GOOGLE_CLIENT_SECRET, GOOGLE_REFRESH_TOKEN), so no one has to paste hourly tokens. The token covers the scopes granted
# when the refresh token was made (blogger and webmasters). It is cached in memory only, never written to disk.
#   python3 seo/google_token.py      (checks that the keys work)
import json, os, time, urllib.parse, urllib.request
_cache = {}

def access_token():
    if _cache.get("exp", 0) > time.time() + 60: return _cache["tok"]
    keys = ("GOOGLE_CLIENT_ID", "GOOGLE_CLIENT_SECRET", "GOOGLE_REFRESH_TOKEN")
    if not all(os.environ.get(k) for k in keys): return None
    data = urllib.parse.urlencode({"client_id": os.environ["GOOGLE_CLIENT_ID"], "client_secret": os.environ["GOOGLE_CLIENT_SECRET"],
                                   "refresh_token": os.environ["GOOGLE_REFRESH_TOKEN"], "grant_type": "refresh_token"}).encode()
    r = json.loads(urllib.request.urlopen("https://oauth2.googleapis.com/token", data, timeout=30).read())
    _cache.update(tok=r["access_token"], exp=time.time() + int(r.get("expires_in", 3600)), scope=r.get("scope", ""))
    return _cache["tok"]

if __name__ == "__main__":
    t = access_token()
    print("Google keys work. Scopes:", _cache.get("scope")) if t else print("GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET and GOOGLE_REFRESH_TOKEN are not all set")
