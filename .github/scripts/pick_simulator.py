#!/usr/bin/env python3
"""En yeni iOS sürümünde bir iPhone simülatörü seçer ve "UDID|ad|sürüm" yazar.

Tasarım 390 × 844 pt'lik bir ekranda çizildi; aynı boyuttaki iPhone 16e tercih edilir.
Yoksa oluşturulmaya çalışılır, olmazsa listedeki ilk uygun iPhone seçilir.
"""

import json
import re
import subprocess
import sys

PREFERRED = ["iPhone 16e", "iPhone 17", "iPhone 16", "iPhone 17 Pro", "iPhone 16 Pro", "iPhone 15"]


def ios_version(runtime_id):
    match = re.search(r"iOS-(\d+)-(\d+)", runtime_id)
    return (int(match.group(1)), int(match.group(2))) if match else None


def main():
    listing = subprocess.check_output(["xcrun", "simctl", "list", "devices", "available", "-j"])
    devices = json.loads(listing)["devices"]
    runtimes = sorted((ios_version(rid), rid) for rid in devices if ios_version(rid) and devices[rid])
    if not runtimes:
        sys.exit("iOS simülatörü bulunamadı")
    version, runtime = runtimes[-1]
    phones = {d["name"]: d["udid"] for d in devices[runtime] if d["name"].startswith("iPhone")}

    if "iPhone 16e" not in phones:
        try:
            udid = subprocess.check_output(
                ["xcrun", "simctl", "create", "iPhone 16e", "com.apple.CoreSimulator.SimDeviceType.iPhone-16e", runtime],
                stderr=subprocess.DEVNULL,
            ).decode().strip()
            phones["iPhone 16e"] = udid
        except subprocess.CalledProcessError:
            pass

    name = next((n for n in PREFERRED if n in phones), None) or sorted(phones)[0]
    print(f"{phones[name]}|{name}|iOS {version[0]}.{version[1]}")


if __name__ == "__main__":
    main()
