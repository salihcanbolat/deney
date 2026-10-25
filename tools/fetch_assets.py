#!/usr/bin/env python3
"""Parti logolarını ve lider fotoğraflarını Wikimedia Commons'tan indirip asset kataloğuna yazar.

Kaynak listesi: tools/party_assets.json
  { "assets": [ { "name": "logo-mhp", "file": "Milliyetçi Hareket Partisi Logo.svg", "width": 512 }, ... ] }
  "wiki": "tr" verilirse dosya Commons yerine Türkçe Vikipedi'den alınır.

Her görsel App/Resources/Assets.xcassets/Parties/<name>.imageset/ altına yazılır.
Wikimedia, Special:FilePath üzerinden SVG'leri istenen genişlikte PNG'ye çevirir.
Bu betik GitHub Actions'ta çalışır (.github/workflows/assets.yml); bulut oturumunun
ağı Wikimedia'ya izin vermiyor.

Kullanım: python3 tools/fetch_assets.py
"""

import json
import sys
import time
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SOURCES = ROOT / "tools/party_assets.json"
CATALOG = ROOT / "App/Resources/Assets.xcassets/Parties"
USER_AGENT = "SecimAnketi/0.1 (https://github.com/salihcanbolat/deney; asset fetcher)"  # HTTP başlığı yalnızca ASCII


def download(file_name, width, wiki):
    host = "tr.wikipedia.org" if wiki == "tr" else "commons.wikimedia.org"
    url = f"https://{host}/wiki/Special:FilePath/" + urllib.parse.quote(file_name) + f"?width={width}"
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=60) as response:
        return response.read(), response.headers.get("Content-Type", "")


def extension(content_type, data):
    if "png" in content_type or data[:8] == b"\x89PNG\r\n\x1a\n":
        return "png"
    if "jpeg" in content_type or data[:3] == b"\xff\xd8\xff":
        return "jpg"
    raise ValueError(f"beklenmeyen içerik türü: {content_type}")


def process(data, ext, crop, is_photo):
    """Fotoğrafı yüze göre kare kırpar (360 px JPEG), logoyu en çok 512 px'e küçültür."""
    from io import BytesIO
    from PIL import Image

    image = Image.open(BytesIO(data))
    if is_photo:
        image = image.convert("RGB")
        w, h = image.size
        if crop == "auto":
            side = 0.8 * min(w, h)
            cx, cy = w / 2, side / 2 + 0.04 * h
        elif crop:
            cx, cy, side = crop[0] * w, crop[1] * h, crop[2] * w
        else:
            side = min(w, h)
            cx, cy = w / 2, side / 2
        side = min(side, w, h)
        left = min(max(cx - side / 2, 0), w - side)
        top = min(max(cy - side / 2, 0), h - side)
        image = image.crop((round(left), round(top), round(left + side), round(top + side)))
        image = image.resize((360, 360), Image.LANCZOS)
        out = BytesIO()
        image.save(out, "JPEG", quality=88, optimize=True)
        return out.getvalue(), "jpg"
    image.thumbnail((512, 512), Image.LANCZOS)
    out = BytesIO()
    image.save(out, "PNG", optimize=True)
    return out.getvalue(), "png"


def write_imageset(name, data, ext):
    folder = CATALOG / f"{name}.imageset"
    folder.mkdir(parents=True, exist_ok=True)
    for old in folder.iterdir():
        old.unlink()
    filename = f"{name}.{ext}"
    (folder / filename).write_bytes(data)
    contents = {
        "images": [{"filename": filename, "idiom": "universal"}],
        "info": {"author": "xcode", "version": 1},
    }
    (folder / "Contents.json").write_text(json.dumps(contents, indent=2) + "\n", encoding="utf-8")


def main():
    CATALOG.mkdir(parents=True, exist_ok=True)
    folder_contents = CATALOG / "Contents.json"
    if not folder_contents.exists():
        folder_contents.write_text(json.dumps({"info": {"author": "xcode", "version": 1}}, indent=2) + "\n", encoding="utf-8")

    assets = json.loads(SOURCES.read_text(encoding="utf-8"))["assets"]
    failures = []
    for item in assets:
        name, width = item["name"], item.get("width", 512)
        for candidate in item["candidates"]:
            try:
                data, content_type = download(candidate["file"], width, candidate.get("wiki"))
                ext = extension(content_type, data)
                data, ext = process(data, ext, candidate.get("crop"), name.startswith("leader-"))
                write_imageset(name, data, ext)
                print(f"tamam  {name:22} {len(data) // 1024:4} KB  {candidate['file']}")
                break
            except Exception as error:  # sıradaki adayı dene
                print(f"  yok  {name:22} {candidate['file']}: {error}")
            finally:
                time.sleep(0.4)
        else:
            failures.append(name)

    if failures:
        print(f"\nİndirilemeyenler: {', '.join(failures)}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
