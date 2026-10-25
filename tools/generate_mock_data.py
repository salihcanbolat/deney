#!/usr/bin/env python3
"""Uygulama verisi üreticisi: gerçek partiler, sembolik sonuçlar.

Kullanıcı kararı (25 Ekim 2026, CLAUDE.md Bölüm 15.14): gerçek partiler, genel başkanlar,
logolar ve fotoğraflar kullanılır; oranlar uydurulmaz. Gerçek oylar backend gelene kadar
(Aşama 6) tüm partiler EŞİT ve SEMBOLİK değer alır; arayüz oran göstermez, "Sembolik" yazar.

Sıralama: TBMM sandalye sayısı (NTV, 20 Ekim 2026). Oy oranına göre değildir.
Genel başkanlar: 25 Ekim 2026 itibarıyla haber kaynaklarından doğrulandı; değişebilir.

Kullanım: python3 tools/generate_mock_data.py
"""

import json
from pathlib import Path

OUT = Path(__file__).resolve().parent.parent / "Packages/PollCore/Sources/PollCore/Resources/mock-data.json"

# kimlik | kısa ad | resmî ad | genel başkan(lar) | yer tutucu rengi | sandalye | logo asset | lider foto asset(leri)
PARTIES = [
    ("akp", "AK PARTİ", "Adalet ve Kalkınma Partisi", "Recep Tayyip Erdoğan", "#F39200", 281, "logo-akp", ["leader-erdogan"]),
    ("yeni", "YENİ PARTİ", "YENİ Parti", "Özgür Özel", "#E30A17", 91, "logo-yeni", ["leader-ozel"]),
    ("dem", "DEM PARTİ", "Halkların Eşitlik ve Demokrasi Partisi", "Tülay Hatimoğulları Oruç & Tuncer Bakırhan", "#6C2C91", 56, "logo-dem", ["leader-hatimogullari"]),
    ("mhp", "MHP", "Milliyetçi Hareket Partisi", "Devlet Bahçeli", "#E30A17", 46, "logo-mhp", ["leader-bahceli"]),
    ("chp", "CHP", "Cumhuriyet Halk Partisi", "Kemal Kılıçdaroğlu", "#ED1C24", 44, "logo-chp", ["leader-kilicdaroglu"]),
    ("iyi", "İYİ PARTİ", "İYİ Parti", "Müsavat Dervişoğlu", "#00A0E3", 27, "logo-iyi", ["leader-dervisoglu"]),
    ("yeniyol", "YENİ YOL", "Yeni Yol Partisi", "İzzettin Küçük", "#1D4E89", 20, None, []),
    ("hudapar", "HÜDA PAR", "Hür Dava Partisi", "Zekeriya Yapıcıoğlu", "#0C7A3E", 4, None, ["leader-yapicioglu"]),
    ("yrp", "YRP", "Yeniden Refah Partisi", "Fatih Erbakan", "#D71920", 3, "logo-yrp", ["leader-erbakan"]),
    ("tip", "TİP", "Türkiye İşçi Partisi", "Erkan Baş", "#C8102E", 3, "logo-tip", ["leader-bas"]),
    ("dbp", "DBP", "Demokratik Bölgeler Partisi", "Çiğdem Kılıçgün Uçar & Keskin Bayındır", "#2E7D32", 2, "logo-dbp", ["leader-ucar"]),
    ("emep", "EMEP", "Emek Partisi", "Seyit Aslan", "#C62828", 2, "logo-emep", []),
    ("saadet", "SAADET", "Saadet Partisi", "Mahmut Arıkan", "#D32F2F", 1, "logo-saadet", []),
    ("dsp", "DSP", "Demokratik Sol Parti", "Önder Aksakal", "#1565C0", 1, "logo-dsp", []),
    ("dp", "DP", "Demokrat Parti", "Gültekin Uysal", "#C62828", 1, None, ["leader-uysal"]),
    ("zafer", "ZAFER", "Zafer Partisi", "Ümit Özdağ", "#B71C1C", 0, "logo-zafer", ["leader-ozdag"]),
    ("anahtar", "ANAHTAR", "Anahtar Parti", "Yavuz Ağıralioğlu", "#0D47A1", 0, "logo-anahtar", ["leader-agiralioglu"]),
]

# Kendi zemini olan (kare ya da tam daire) logolar rozeti doldurur; diğerleri beyaz rozetin içine oturur.
FILL_LOGOS = {"logo-chp", "logo-iyi", "logo-saadet", "logo-yrp", "logo-dbp", "logo-mhp", "logo-dem", "logo-tip"}

# Herkese aynı değer: hiçbir parti önde ya da geride görünmez.
SYMBOLIC = 10.0
UPDATED_AT = "2026-10-25T09:41:00+03:00"


def main():
    parties = []
    for pid, code, name, leader, color, seats, logo, photos in PARTIES:
        party = {"id": pid, "code": code, "name": name, "leaderName": leader, "color": color, "seats": seats}
        if logo:
            party["logoAsset"] = logo
            if logo in FILL_LOGOS:
                party["logoFills"] = True
        if photos:
            party["leaderPhotoAssets"] = photos
        parties.append(party)
    ids = [p["id"] for p in parties]

    polls = [
        {"id": "genel-secim", "title": "Genel Seçim Anketi", "type": "supportAndNever", "includesLeader": True},
        {"id": "genc-secmen", "title": "Genç Seçmen Anketi", "type": "supportAndNever", "includesLeader": True},
        {"id": "lider-tercihi", "title": "Lider Tercihi Anketi", "type": "support", "includesLeader": True},
        {"id": "asla-oy-vermem", "title": "Asla Oy Vermem Anketi", "type": "never", "includesLeader": False},
    ]
    for poll in polls:
        poll.update({"participantCount": 0, "isLive": False, "partyIds": ids})

    def row(pid, poll):
        out = {"partyId": pid}
        if poll["type"] != "never":
            out["partyPos"] = SYMBOLIC
            if poll["includesLeader"]:
                out["leaderPos"] = SYMBOLIC
        if poll["type"] != "support":
            out["partyNeg"] = SYMBOLIC
            if poll["includesLeader"]:
                out["leaderNeg"] = SYMBOLIC
        return out

    results = [
        {"pollId": poll["id"], "updatedAt": UPDATED_AT, "isSymbolic": True, "rows": [row(pid, poll) for pid in ids]}
        for poll in polls
    ]

    data = {
        "note": "Gerçek partiler ve genel başkanlar (25 Ekim 2026). Oranlar sembolik ve eşittir; gerçek oy değildir. Sıra: TBMM sandalye sayısı.",
        "parties": parties,
        "polls": polls,
        "results": results,
        "sampleUser": {"prefs": {"followedPollIds": ["genel-secim"], "lockScreenPollId": "genel-secim", "notificationsByPoll": {}}, "ballots": []},
    }
    OUT.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"yazıldı: {OUT}")


if __name__ == "__main__":
    main()
