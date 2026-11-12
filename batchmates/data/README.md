# BatchMates school & college directory

Static, gzip-compressed JSON files the app downloads on demand (one ~55 km map cell at a time) and caches.
Built by `tools/build_directory.py` from **Overture Maps Places** (release 2026-10-21.0).

- `schools/<CC>/<latCell>_<lonCell>.json.gz` — `{"v":1,"s":[[id, name, lat, lon, kind, town], …]}`, cell = 0.5°
- `towns/<CC>.json.gz` — `{"v":1,"t":[[name, lat, lon, schools, region], …]}`

Served free by jsDelivr / GitHub raw from this public repository. To move to Cloudflare R2 or Firebase Hosting,
upload this folder and set `batchmates.dataMirrors` in `gradle.properties`.

Data © Overture Maps Foundation contributors, CDLA-Permissive-2.0 (https://docs.overturemaps.org/attribution/).
