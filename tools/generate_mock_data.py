#!/usr/bin/env python3
"""Örnek veri üreticisi.

Tüm partiler, liderler ve rakamlar uydurmadır (CLAUDE.md, Bölüm 2).

Kaynaklar:
  - 1–8. partilerin dört değeri: CLAUDE.md Bölüm 9 tablosu.
  - 9–40. partilerin parti ▲ / parti ▼ değerleri: design-reference/v1-eski-taslak/Main.dc.html.
  - Genç Seçmen Anketi'nin ilk 8 parti/lider ▲ değerleri: design-reference/v2-onaylanan/Polls.dc.html.
  - Hiçbir dosyada olmayan değerler (9–40. partilerin lider değerleri, diğer anketler)
    sabit tohumlu sapmalarla türetilir; her çalıştırmada aynı çıktı üretilir.

Kullanım: python3 tools/generate_mock_data.py
"""

import json
import random
from pathlib import Path

OUT = Path(__file__).resolve().parent.parent / "Packages/PollCore/Sources/PollCore/Resources/mock-data.json"

# kod | parti | lider | logo rengi | parti ▲ | parti ▼
PARTIES = """
YUP|Yeni Ufuk Partisi|Selin Arda|#2F5FD0|16.4|22.1
ADP|Anadolu Demokrasi Partisi|Kemal Tunç|#B0441A|15.9|23.8
BHP|Birlik ve Huzur Partisi|Murat Ergin|#0E6F57|11.2|9.2
HLK|Halkın Kalkınma Partisi|Ayşe Doğan|#B42D45|8.7|6.4
ÇEP|Çevre ve Emek Partisi|Deniz Kaya|#3B7F2A|6.3|3.1
GSP|Gelecek Sesi Partisi|Emre Yalın|#6A44C2|5.1|4.8
MİD|Milli Dayanışma Partisi|Hakan Sezer|#7A5C0F|4.4|7.9
ÖTP|Özgür Toplum Partisi|Leyla Aksoy|#A8327A|3.8|2.2
KAP|Kıyı ve Akdeniz Partisi|Orhan Bilge|#1F6F8C|3.1|1.9
YDP|Yeşil Dönüşüm Partisi|Nazlı Er|#276749|2.7|1.4
EGP|Eşit Gelecek Partisi|Tarık Uslu|#4B5BB5|2.4|2.6
SNA|Sanayi ve Atılım Partisi|Cengiz Özbek|#7A4A2A|2.1|1.7
TBP|Toprak ve Bereket Partisi|Fatma Ilgaz|#586A12|1.9|1.1
GKP|Gençlik ve Kültür Partisi|Arda Soylu|#B83280|1.7|0.9
HAD|Hukuk ve Adalet Partisi|Zeynep Akalın|#34495E|1.5|1.3
DNZ|Deniz ve Emek Partisi|Barış Yüce|#0B6A8C|1.3|0.8
KYP|Kent ve Yaşam Partisi|Gül Erkan|#A0522D|1.2|0.7
BİP|Bilim ve İnovasyon Partisi|Onur Tekin|#5A3FA0|1.1|0.6
ÜRP|Üretim Partisi|Sinan Polat|#8A3324|1.0|1.0
ÇAP|Çağdaş Atılım Partisi|Pelin Ateş|#2B6CB0|0.9|1.5
YOP|Yol Partisi|Erdem Kılıç|#8C5A12|0.8|0.5
SİP|Sivil İrade Partisi|Hande Koç|#6B46C1|0.7|0.4
ESP|Esnaf ve Sanatkâr Partisi|Yavuz Tan|#9C4221|0.7|0.6
KİP|Köy İnsanları Partisi|Melis Yurt|#2C7A7B|0.6|0.3
OTP|Ortak Toplum Partisi|Rıza Bozkurt|#744210|0.6|0.9
DÖP|Dönüşüm Partisi|Ece Turan|#9D2F6B|0.5|0.3
BYP|Bağımsız Yurttaş Partisi|Kaan Eren|#4A5568|0.5|0.4
TEP|Teknoloji Partisi|İpek Kalkan|#2C5282|0.4|0.2
ÇİP|Çiftçi Partisi|Halil Duman|#556F1C|0.4|0.3
UYP|Uyum Partisi|Sevgi Çınar|#97266D|0.4|0.5
AİP|Aile ve İnsan Partisi|Ahmet Gök|#975A16|0.3|0.4
GÜP|Güneş Partisi|Selda Uz|#A3401A|0.3|0.2
DOP|Dağ ve Ova Partisi|İlker Saran|#22543D|0.3|0.2
MEP|Meclis Etkinlik Partisi|Burcu Aydın|#553C9A|0.2|0.3
KUP|Kuzey Partisi|Ozan Çelik|#2A4365|0.2|0.1
GÖP|Gönüllüler Partisi|Nil Sarı|#9B2C2C|0.2|0.2
YEP|Yaşlılar ve Emekliler Partisi|Şükrü Ak|#4C51BF|0.2|0.1
SÇP|Sessiz Çoğunluk Partisi|Aslı Var|#5A6578|0.1|0.1
İDP|İklim ve Doğa Partisi|Ufuk Ser|#2F6B4F|0.1|0.1
BEP|Barış Ekseni Partisi|Tuna Er|#822727|0.1|0.2
"""

# Bölüm 9 tablosu: lider ▲ ve lider ▼ (ilk 8 parti)
GENERAL_LEADERS_TOP8 = [(18.9, 25.4), (13.2, 26.9), (14.8, 7.8), (7.1, 8.1), (8.4, 2.6), (4.2, 5.5), (5.6, 6.2), (3.1, 2.9)]

# Polls.dc.html: Genç Seçmen Anketi, ilk 8 parti (parti ▲, lider ▲)
YOUTH_TOP8 = [(14, 16), (12, 11), (13, 15), (9, 8), (9, 11), (6, 5), (3, 4), (5, 4)]

UPDATED_AT = "2026-10-25T09:41:00+03:00"


def jitter(rng, values, low, high):
    return [v * rng.uniform(low, high) for v in values]


def fill_to(values, target, cap=None):
    """Değerleri toplamları `target` olacak şekilde ölçekler; `cap` verilirse tavanı aşanları kırpıp farkı dağıtır."""
    values = list(values)
    for _ in range(20):
        total = sum(values)
        values = [v * target / total for v in values]
        if cap is None or max(values) <= cap:
            break
        excess = sum(v - cap for v in values if v > cap)
        values = [min(v, cap) for v in values]
        free = [i for i, v in enumerate(values) if v < cap]
        share = sum(values[i] for i in free)
        for i in free:
            values[i] += excess * values[i] / share
    return values


def r1(v):
    return round(v + 1e-9, 1)


def row(party_id, participants, pp=None, lp=None, pn=None, ln=None):
    out = {"partyId": party_id}
    for key, value in (("partyPos", pp), ("leaderPos", lp), ("partyNeg", pn), ("leaderNeg", ln)):
        if value is not None:
            out[key] = r1(value)
            out[key + "Count"] = round(r1(value) / 100 * participants)
    return out


def sort_rows(rows):
    return sorted(rows, key=lambda r: -(r.get("partyPos", r.get("partyNeg", 0))))


def main():
    rng = random.Random(20261025)
    parsed = [line.split("|") for line in PARTIES.strip().splitlines()]
    assert len(parsed) == 40
    ids = [f"p{i:02d}" for i in range(1, 41)]
    parties = [
        {"id": pid, "code": f[0], "name": f[1], "leaderName": f[2], "color": f[3]}
        for pid, f in zip(ids, parsed)
    ]
    party_pos = [float(f[4]) for f in parsed]
    party_neg = [float(f[5]) for f in parsed]

    # Genel Seçim Anketi
    total_pos, total_neg = sum(party_pos), sum(party_neg)
    lp_top = [l for l, _ in GENERAL_LEADERS_TOP8]
    ln_top = [l for _, l in GENERAL_LEADERS_TOP8]
    lp_rest = fill_to(jitter(rng, party_pos[8:], 0.75, 1.25), total_pos - sum(lp_top), cap=3.0)
    ln_rest = fill_to(jitter(rng, party_neg[8:], 0.75, 1.25), total_neg - sum(ln_top), cap=2.0)
    general_lp = lp_top + lp_rest
    general_ln = ln_top + ln_rest

    polls = [
        {"id": "genel-secim", "title": "Genel Seçim Anketi", "type": "supportAndNever", "includesLeader": True,
         "participantCount": 128460, "isLive": True, "partyIds": ids},
        {"id": "genc-secmen", "title": "Genç Seçmen Anketi", "type": "supportAndNever", "includesLeader": True,
         "participantCount": 23118, "isLive": True, "partyIds": ids},
        {"id": "lider-tercihi", "title": "Lider Tercihi Anketi", "type": "support", "includesLeader": True,
         "participantCount": 84215, "isLive": True, "partyIds": ids},
        {"id": "asla-oy-vermem", "title": "Asla Oy Vermem Anketi", "type": "never", "includesLeader": False,
         "participantCount": 61902, "isLive": True, "partyIds": ids},
    ]
    n = {p["id"]: p["participantCount"] for p in polls}

    general = [row(ids[i], n["genel-secim"], party_pos[i], general_lp[i], party_neg[i], general_ln[i]) for i in range(40)]

    # Genç Seçmen Anketi: ilk 8 Polls.dc.html'den, kalanlar 3'ün altında kalacak şekilde türetilir
    y_pp_top = [p for p, _ in YOUTH_TOP8]
    y_lp_top = [l for _, l in YOUTH_TOP8]
    y_pp_rest = fill_to(jitter(rng, party_pos[8:], 0.8, 1.2), 100 - sum(y_pp_top), cap=2.9)
    y_lp_rest = fill_to(jitter(rng, general_lp[8:], 0.8, 1.2), 100 - sum(y_lp_top), cap=2.9)
    y_pn = fill_to(jitter(rng, party_neg, 0.7, 1.3), 100)
    y_ln = fill_to(jitter(rng, general_ln, 0.7, 1.3), 100)
    y_pp, y_lp = y_pp_top + y_pp_rest, y_lp_top + y_lp_rest
    youth = [row(ids[i], n["genc-secmen"], y_pp[i], y_lp[i], y_pn[i], y_ln[i]) for i in range(40)]

    # Lider Tercihi Anketi: yalnız destek
    l_pp = fill_to(jitter(rng, party_pos, 0.85, 1.15), 100)
    l_lp = fill_to(jitter(rng, general_lp, 0.85, 1.15), 100)
    leader = [row(ids[i], n["lider-tercihi"], pp=l_pp[i], lp=l_lp[i]) for i in range(40)]

    # Asla Oy Vermem Anketi: yalnız asla, lider bölümü yok
    a_pn = fill_to(jitter(rng, party_neg, 0.85, 1.15), 100)
    never = [row(ids[i], n["asla-oy-vermem"], pn=a_pn[i]) for i in range(40)]

    results = [
        {"pollId": pid, "updatedAt": UPDATED_AT, "rows": sort_rows(rows)}
        for pid, rows in (("genel-secim", general), ("genc-secmen", youth), ("lider-tercihi", leader), ("asla-oy-vermem", never))
    ]

    code_to_id = {p["code"]: p["id"] for p in parties}
    sample_user = {
        "prefs": {
            "followedPollIds": ["genel-secim", "genc-secmen"],
            "lockScreenPollId": "genel-secim",
            "notificationsByPoll": {"genel-secim": True, "genc-secmen": True},
        },
        "ballots": [
            {"pollId": "genel-secim", "partyYes": code_to_id["BHP"], "leaderYes": code_to_id["BHP"],
             "never": code_to_id["ADP"], "castAt": "2026-10-24T20:15:00+03:00"},
            {"pollId": "genc-secmen", "partyYes": code_to_id["BHP"], "leaderYes": code_to_id["BHP"],
             "never": code_to_id["ADP"], "castAt": "2026-10-24T20:18:00+03:00"},
        ],
    }

    data = {
        "note": "Tüm partiler, liderler ve rakamlar uydurmadır. Temsili anket / örnek veri.",
        "parties": parties,
        "polls": polls,
        "results": results,
        "sampleUser": sample_user,
    }
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"yazıldı: {OUT}")


if __name__ == "__main__":
    main()
